// System Status Monitor JavaScript
let allSystems = [];
let filteredSystems = [];
let currentFilter = 'all';
let currentShipCodeFilter = 'all';
let currentView = 'grid';
let autoRefreshInterval = null;
let nextRefreshSeconds = 900;
let lastScrollTop = 0;
let scrollThreshold = 100;

// Suppressed ship codes — persisted to localStorage
const SUPPRESS_KEY = 'endpoints_suppressed_ships';
let suppressedShipCodes = new Set(JSON.parse(localStorage.getItem(SUPPRESS_KEY) || '[]'));

function saveSuppressed() {
    localStorage.setItem(SUPPRESS_KEY, JSON.stringify([...suppressedShipCodes]));
}

function addSuppressedShip(code) {
    if (!code) return;
    suppressedShipCodes.add(code.toUpperCase());
    saveSuppressed();
    renderSuppressedTags();
    populateSuppressDropdown();
    applyFilters();
    updateSummary();
}

function removeSuppressedShip(code) {
    suppressedShipCodes.delete(code);
    saveSuppressed();
    renderSuppressedTags();
    populateSuppressDropdown();
    applyFilters();
    updateSummary();
}

function renderSuppressedTags() {
    const container = document.getElementById('suppressedTags');
    if (!container) return;
    if (suppressedShipCodes.size === 0) {
        container.innerHTML = '';
        return;
    }
    container.innerHTML = [...suppressedShipCodes].sort().map(code => `
        <span class="suppressed-tag">
            ${code}
            <button class="suppressed-tag-remove" onclick="removeSuppressedShip('${code}')" title="Unsuppress ${code}">&times;</button>
        </span>
    `).join('');
}

function populateSuppressDropdown() {
    const sel = document.getElementById('suppressShipSelect');
    if (!sel) return;
    const codes = Array.from(new Set(
        allSystems.map(s => normalizeShipCode(s.ssmShipCode)).filter(Boolean)
    )).sort();
    sel.innerHTML = '<option value="">Do not show</option>';
    codes.filter(c => !suppressedShipCodes.has(c)).forEach(code => {
        const opt = document.createElement('option');
        opt.value = code;
        opt.textContent = code;
        sel.appendChild(opt);
    });
    sel.value = '';
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    initializeEventListeners();
    loadSystemData();
    startAutoRefresh();
    initializeAutoHideHeader();
});

// Initialize event listeners
function initializeEventListeners() {
    // Search input
    const searchInput = document.getElementById('searchInput');
    searchInput.addEventListener('input', handleSearch);

    const shipCodeFilter = document.getElementById('shipCodeFilter');
    if (shipCodeFilter) {
        shipCodeFilter.addEventListener('change', function () {
            currentShipCodeFilter = this.value;
            applyFilters();
        });
    }

    const suppressSel = document.getElementById('suppressShipSelect');
    if (suppressSel) {
        suppressSel.addEventListener('change', function () {
            addSuppressedShip(this.value);
            this.value = '';
        });
    }
    
    // Filter buttons
    const filterButtons = document.querySelectorAll('.filter-btn');
    filterButtons.forEach(btn => {
        btn.addEventListener('click', function() {
            filterButtons.forEach(b => b.classList.remove('active'));
            this.classList.add('active');
            currentFilter = this.dataset.filter;
            applyFilters();
        });
    });
    
    // View toggle buttons
    const viewButtons = document.querySelectorAll('.view-btn');
    viewButtons.forEach(btn => {
        btn.addEventListener('click', function() {
            viewButtons.forEach(b => b.classList.remove('active'));
            this.classList.add('active');
            currentView = this.dataset.view;
            updateView();
        });
    });
}

// Initialize auto-hide header on scroll
function initializeAutoHideHeader() {
    const header = document.querySelector('header');
    let isScrolling;
    
    window.addEventListener('scroll', function() {
        // Clear timeout throughout the scroll
        window.clearTimeout(isScrolling);
        
        const currentScrollTop = window.pageYOffset || document.documentElement.scrollTop;
        
        // Scrolling down & past threshold
        if (currentScrollTop > lastScrollTop && currentScrollTop > scrollThreshold) {
            header.classList.add('hidden');
        } 
        // Scrolling up
        else if (currentScrollTop < lastScrollTop) {
            header.classList.remove('hidden');
        }
        
        lastScrollTop = currentScrollTop <= 0 ? 0 : currentScrollTop;
        
        // Set a timeout to run after scrolling ends
        isScrolling = setTimeout(function() {
            // Show header when scroll stops
            header.classList.remove('hidden');
        }, 150);
    }, false);
}

// Load system data from API
async function loadSystemData() {
    try {
        showLoading();
        
        const response = await fetch('/endpoints-ship/api/status');
        
        if (!response.ok) {
            // Try to get error details from response
            try {
                const errorData = await response.json();
                if (errorData.error) {
                    throw new Error(errorData.message || errorData.error);
                }
            } catch (e) {
                // If parsing fails, throw generic error
            }
            throw new Error(`Server returned ${response.status}: ${response.statusText}`);
        }
        
        const data = await response.json();
        
        // Check if response is an error object
        if (data.error) {
            throw new Error(data.message || 'Database connection failed');
        }
        
        // Ensure data is an array
        allSystems = Array.isArray(data) ? data : [];
        console.log(`Loaded ${allSystems.length} systems from API`);

        populateShipCodeFilter();
        populateSuppressDropdown();
        renderSuppressedTags();
        applyFilters();
        updateSummary();
        updateLastUpdate();
        
    } catch (error) {
        console.error('Error loading system data:', error);
        const errorMsg = error.message.includes('Database') || error.message.includes('connectivity')
            ? '⚠️ Database Connection Error<br/><small>Unable to connect to the database. Please verify the database is running and accessible.</small>'
            : '⚠️ Failed to Load Data<br/><small>' + error.message + '</small>';
        showError(errorMsg);
    }
}

// Show loading state
function showLoading() {
    const container = document.getElementById('systemsContainer');
    container.innerHTML = '<div class="loading">Loading systems</div>';
}

// Show error message
function showError(message) {
    const container = document.getElementById('systemsContainer');
    container.innerHTML = `
        <div class="empty-state">
            <div class="empty-state-icon">⚠️</div>
            <div class="empty-state-text">${message}</div>
        </div>
    `;
}

// Apply filters
function applyFilters() {
    const searchTerm = document.getElementById('searchInput').value.toLowerCase();
    const selectedShipCode = normalizeShipCode(currentShipCodeFilter);
    
    filteredSystems = allSystems.filter(system => {
        // Filter out suppressed ships
        if (suppressedShipCodes.has(normalizeShipCode(system.ssmShipCode))) return false;

        // Filter by ship code
        let shipCodeMatch = true;
        if (selectedShipCode && selectedShipCode !== 'ALL') {
            shipCodeMatch = normalizeShipCode(system.ssmShipCode) === selectedShipCode;
        }

        // Filter by status
        let statusMatch = true;
        if (currentFilter === 'running') {
            statusMatch = isRunning(system.ssmSystemStatus);
        } else if (currentFilter === 'outage') {
            statusMatch = isOutage(system.ssmSystemStatus);
        } else if (currentFilter === 'other') {
            statusMatch = !isRunning(system.ssmSystemStatus) && !isOutage(system.ssmSystemStatus);
        }
        
        // Filter by search term
        let searchMatch = true;
        if (searchTerm) {
            const normalizedSearchTerm = searchTerm.trim().toUpperCase();
            searchMatch = 
                (normalizeShipCode(system.ssmShipCode).includes(normalizedSearchTerm)) ||
                (system.ssmSystemName && system.ssmSystemName.toLowerCase().includes(searchTerm)) ||
                (system.ssmSystemType && system.ssmSystemType.toLowerCase().includes(searchTerm));
        }
        
        return shipCodeMatch && statusMatch && searchMatch;
    });
    
    renderSystems();
    updateVisibleCount();
}

// Handle search input
function handleSearch() {
    applyFilters();
}

function populateShipCodeFilter() {
    const shipCodeFilter = document.getElementById('shipCodeFilter');
    if (!shipCodeFilter) return;

    const currentValue = currentShipCodeFilter || 'all';
    const shipCodes = Array.from(new Set(
        allSystems
            .map(system => normalizeShipCode(system.ssmShipCode))
            .filter(code => !!code)
    )).sort((left, right) => left.localeCompare(right));

    shipCodeFilter.innerHTML = '<option value="all">All Ship Codes</option>';
    shipCodes.forEach(code => {
        const option = document.createElement('option');
        option.value = code;
        option.textContent = code;
        shipCodeFilter.appendChild(option);
    });

    shipCodeFilter.value = shipCodes.includes(currentValue) ? currentValue : 'all';
    currentShipCodeFilter = shipCodeFilter.value;
}

// Render systems
function renderSystems() {
    const container = document.getElementById('systemsContainer');
    
    if (filteredSystems.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">🔍</div>
                <div class="empty-state-text">No systems found matching your criteria</div>
            </div>
        `;
        return;
    }
    
    container.className = `systems-container ${currentView}-view`;
    
    container.innerHTML = filteredSystems.map(system => createSystemCard(system)).join('');
    
    // Add click listeners to cards
    container.querySelectorAll('.system-card').forEach((card, index) => {
        card.addEventListener('click', () => showSystemDetail(filteredSystems[index]));
    });
}

// Create system card HTML
function createSystemCard(system) {
    const statusClass = getStatusClass(system.ssmSystemStatus);
    const statusDisplay = getStatusDisplay(system.ssmSystemStatus);
    const formattedDateTime = formatDateTime(system.ssmSystemDateTime);
    const formattedCreatedDate = formatDateTime(system.ssmCreatedDate);
    
    return `
        <div class="system-card" data-id="${system.ssmSuid}">
            <div class="system-card-header">
                <div>
                    <div class="system-name">${escapeHtml(system.ssmSystemName || 'Unknown System')}</div>
                    <div class="system-ship-code">🚢 ${escapeHtml(system.ssmShipCode || 'N/A')} ${system.ssmSystemType ? '• ' + escapeHtml(system.ssmSystemType) : ''}</div>
                </div>
                <div class="status-badge ${statusClass}">${statusDisplay}</div>
            </div>
            <div class="system-info">
                <div class="info-row">
                    <span class="info-label">Last Update:</span>
                    <span class="info-value">${formattedDateTime}</span>
                </div>
                <div class="info-row">
                    <span class="info-label">Created:</span>
                    <span class="info-value">${formattedCreatedDate}</span>
                </div>
            </div>
            ${system.ssmMessage ? `<div class="system-message">💬 ${escapeHtml(system.ssmMessage)}</div>` : ''}
        </div>
    `;
}

// Update view
function updateView() {
    renderSystems();
}

// Update summary statistics
function updateSummary() {
    const total = allSystems.length;
    const running = allSystems.filter(s => isRunning(s.ssmSystemStatus)).length;
    const outage = allSystems.filter(s => isOutage(s.ssmSystemStatus)).length;
    const other = total - running - outage;
    
    document.getElementById('totalSystems').textContent = total;
    document.getElementById('runningCount').textContent = running;
    document.getElementById('outageCount').textContent = outage;
    document.getElementById('otherCount').textContent = other;
}

// Update visible count
function updateVisibleCount() {
    document.getElementById('visibleCount').textContent = filteredSystems.length;
    document.getElementById('totalCount').textContent = allSystems.length;
}

// Update last update time
function updateLastUpdate() {
    const now = new Date();
    const timeStr = now.toLocaleTimeString();
    document.getElementById('lastUpdate').textContent = `Last updated: ${timeStr}`;
}

// Show system detail modal
function showSystemDetail(system) {
    const modal = document.getElementById('detailModal');
    const modalBody = document.getElementById('modalBody');
    
    const statusClass = getStatusClass(system.ssmSystemStatus);
    const statusDisplay = getStatusDisplay(system.ssmSystemStatus);
    
    modalBody.innerHTML = `
        <div class="detail-row">
            <span class="detail-label">System ID:</span>
            <span class="detail-value">${system.ssmSuid || 'N/A'}</span>
        </div>
        <div class="detail-row">
            <span class="detail-label">Ship Code:</span>
            <span class="detail-value">${escapeHtml(system.ssmShipCode || 'N/A')}</span>
        </div>
        <div class="detail-row">
            <span class="detail-label">System Name:</span>
            <span class="detail-value">${escapeHtml(system.ssmSystemName || 'N/A')}</span>
        </div>
        <div class="detail-row">
            <span class="detail-label">System Type:</span>
            <span class="detail-value">${escapeHtml(system.ssmSystemType || 'N/A')}</span>
        </div>
        <div class="detail-row">
            <span class="detail-label">Status:</span>
            <span class="detail-value"><span class="status-badge ${statusClass}">${statusDisplay}</span></span>
        </div>
        <div class="detail-row">
            <span class="detail-label">System Date/Time:</span>
            <span class="detail-value">${formatDateTime(system.ssmSystemDateTime)}</span>
        </div>
        <div class="detail-row">
            <span class="detail-label">Created Date:</span>
            <span class="detail-value">${formatDateTime(system.ssmCreatedDate)}</span>
        </div>
        ${system.ssmMessage ? `
        <div class="detail-row">
            <span class="detail-label">Message:</span>
            <span class="detail-value" style="text-align: left; max-width: 70%; word-break: break-word;">${escapeHtml(system.ssmMessage)}</span>
        </div>
        ` : ''}
    `;
    
    modal.classList.add('active');
}

// Close modal
function closeModal(event) {
    const modal = document.getElementById('detailModal');
    modal.classList.remove('active');
}

// Refresh data
async function refreshData() {
    const btn = document.getElementById('refreshBtn');
    if (btn.classList.contains('refreshing')) return;
    
    btn.classList.add('refreshing');
    console.log('Manual refresh triggered');
    
    await loadSystemData();
    
    // Reset auto-refresh timer
    nextRefreshSeconds = 900;
    
    setTimeout(() => btn.classList.remove('refreshing'), 500);
}

// Auto refresh
function startAutoRefresh() {
    // Update countdown
    setInterval(() => {
        if (nextRefreshSeconds > 0) {
            nextRefreshSeconds--;
            const minutes = Math.floor(nextRefreshSeconds / 60);
            const seconds = nextRefreshSeconds % 60;
            document.getElementById('nextRefresh').textContent = 
                `Next refresh in ${minutes}:${seconds.toString().padStart(2, '0')}`;
        }
    }, 1000);
    
    // Auto refresh every 15 minutes
    setInterval(async () => {
        console.log('Auto-refresh triggered');
        await loadSystemData();
        nextRefreshSeconds = 900;
    }, 900000);
}

// Helper functions
function isRunning(status) {
    if (!status) return false;
    const s = status.toUpperCase();
    return s === 'RUNNING' || s === 'UP';
}

function isOutage(status) {
    if (!status) return false;
    const s = status.toUpperCase();
    return s === 'OUTAGE' || s === 'DOWN';
}

function getStatusClass(status) {
    if (isRunning(status)) return 'running';
    if (isOutage(status)) return 'outage';
    return 'other';
}

function getStatusDisplay(status) {
    if (isRunning(status)) return '✅ Running';
    if (isOutage(status)) return '🔴 Outage';
    return status || 'Unknown';
}

function formatDateTime(dateTime) {
    if (!dateTime) return 'N/A';
    try {
        const date = new Date(dateTime);
        return date.toLocaleString();
    } catch (e) {
        return dateTime;
    }
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function normalizeShipCode(value) {
    return (value || '').toString().trim().toUpperCase();
}
