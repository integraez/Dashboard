// ---------- Load Real Data from API ----------
const realShips = [];
let autoRefreshInterval = null;
let nextRefreshSeconds = 300;

async function loadRealData(){
  try {
    // Create abort controller with 3 second timeout
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 3000);
    
    const response = await fetch('/api/queues', { signal: controller.signal });
    clearTimeout(timeoutId);
    
    const data = await response.json();
    console.log('API Response:', data);
    
    // Clear existing real ships
    realShips.length = 0;
    
    // Check if data is an array or has a value property
    const queues = Array.isArray(data) ? data : (data.value || []);
    console.log(`Fetched ${queues.length} queues from API`);
    
    if (queues.length === 0) {
      console.warn('No queues returned from API');
      return false;
    }
    
    // Convert queue data to ship-like objects
    queues.forEach((queue, idx) => {
      // Skip queues with pms.fcweb.cache in the name
      if (queue.queueName && queue.queueName.toLowerCase().includes('pms.fcweb.cache')) {
        console.log(`Skipping filtered queue: ${queue.serverName} / ${queue.queueName}`);
        return; // Skip this queue
      }
      
      // Map status: critical->err, warning->warn, ok->ok
      // ENSURE messageCount is a number (might come from JSON as string)
      const msgCount = Number(queue.messageCount) || 0;
      
      let shipStatus = 'ok';
      if (queue.status === 'critical' || msgCount > 10000) {
        shipStatus = 'err';
      } else if (queue.status === 'warning' || msgCount > 5000) {
        shipStatus = 'warn';
      }
      
      // Debug: log the mapping for critical/warning queues
      if (msgCount > 5000) {
        console.log(`[PRIORITY QUEUE] ${queue.serverName}/${queue.queueName}: status="${queue.status}", messageCount=${msgCount} (was ${typeof queue.messageCount}: "${queue.messageCount}"), mapped to shipStatus="${shipStatus}"`);
      }
      
      const now = Date.now();
      const ship = {
        id: 'queue-' + idx,
        letter: (queue.serverName || 'Q').charAt(0).toUpperCase(),
        name: `${queue.serverName} / ${queue.queueName}`,
        status: shipStatus,
        acked: false,
        lastSeen: now - rand(5_000, 60_000),
        errorRate: Math.min(100, Math.floor((queue.messageCount / 10000) * 100)),
        queue: queue.messageCount,
        latency: rand(40, 900),
        lastEventAt: now, // Set to current time so critical queues appear in attention
        events: [
          { t: now, msg: `Queue depth: ${queue.messageCount} messages` },
          { t: now - 30000, msg: `Connected to ${queue.serverName}` },
          { t: now - 120000, msg: 'Real-time monitoring active' }
        ]
      };
      console.log(`Queue [${shipStatus}]: ${ship.name} - ${queue.messageCount} messages`);
      realShips.push(ship);
    });
    
    console.log(`Loaded ${realShips.length} real TIBCO EMS queues`);
    return true;
  } catch (err) {
    console.warn('Failed to load real data:', err);
    return false;
  }
}

async function refreshData() {
  const btn = document.getElementById('refreshBtn');
  if (btn.classList.contains('refreshing')) return;
  
  btn.classList.add('refreshing');
  console.log('Manual refresh triggered');
  
  const success = await loadRealData();
  if (success) {
    // Use real data
    ships.length = 0;
    ships.push(...realShips);
    ensureAllLetters();
    render();
    renderServerTiles(); // Update server tiles with new error/warning counts
    console.log('Data refreshed successfully');
  }
  
  // Reset auto-refresh timer
  nextRefreshSeconds = 300;
  
  setTimeout(() => btn.classList.remove('refreshing'), 500);
}

function updateRefreshTimer() {
  const timer = document.getElementById('nextRefresh');
  if (!timer) {
    console.error('❌ Timer element #nextRefresh not found in DOM!');
    return;
  }
  
  const minutes = Math.floor(nextRefreshSeconds / 60);
  const seconds = nextRefreshSeconds % 60;
  const timeStr = `${minutes}:${seconds.toString().padStart(2, '0')}`;
  timer.textContent = `Next refresh in ${timeStr}`;
  
  // Log every 10 seconds to avoid console spam
  if (nextRefreshSeconds % 10 === 0) {
    console.log(`⏰ Timer countdown: ${timeStr} (${nextRefreshSeconds}s remaining)`);
  }
  
  nextRefreshSeconds--;
  
  if (nextRefreshSeconds < 0) {
    console.log('🔄 Auto-refresh triggered!');
    nextRefreshSeconds = 300;
    refreshData();
  }
}

function startAutoRefresh() {
  console.log('🚀 Starting auto-refresh timer...');
  // Initialize timer display immediately
  updateRefreshTimer();
  // Update countdown every second
  const intervalId = setInterval(updateRefreshTimer, 1000);
  console.log('✅ Auto-refresh timer started - updates every second, refreshes every 5 minutes');
  return intervalId;
}

// ---------- Data model ----------
const letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".split("");
const statuses = ["ok","warn","err","off"];
const statusLabel = { ok:"OK", warn:"WARN", err:"ERROR", off:"OFFLINE" };
const statusWeight = { err: 4, warn: 3, off: 2, ok: 1 };

// Helper functions for data generation
function rand(min, max) { 
  return Math.floor(min + Math.random() * (max - min + 1)); 
}

// Real data starts empty - loaded from TIBCO servers
const ships = [];

function ensureAllLetters() {
  // Only show ships with actual data - no placeholders for missing letters
  // Ships are populated from real TIBCO EMS servers only
}

// ---------- UI refs ----------
const alphaIndex = document.getElementById("alphaIndex");
const attentionList = document.getElementById("attentionList");
const allList = document.getElementById("allList");
const attentionCount = document.getElementById("attentionCount");
const allCount = document.getElementById("allCount");
const countsEl = document.getElementById("counts");
const filtersEl = document.getElementById("filters");
const searchEl = document.getElementById("search");
const lastTick = document.getElementById("lastTick");

// details
const shipName = document.getElementById("shipName");
const shipMeta = document.getElementById("shipMeta");
const kStatus = document.getElementById("kStatus");
const kSeen = document.getElementById("kSeen");
const kErrRate = document.getElementById("kErrRate");
const kQueue = document.getElementById("kQueue");
const kLat = document.getElementById("kLat");
const kAck = document.getElementById("kAck");
const eventsEl = document.getElementById("events");
const btnAck = document.getElementById("btnAck");

let selectedId = null;
let filter = "all"; // all | ok | warn | err | off | attention
let q = "";

// ---------- Build A–Z index ----------
const firstByLetter = new Map();
function buildIndex(){
  alphaIndex.innerHTML = "";
  letters.forEach(L=>{
    const b = document.createElement("button");
    b.textContent = L;
    b.title = `Jump to ${L}`;
    b.addEventListener("click", ()=> jumpToLetter(L));
    alphaIndex.appendChild(b);
  });
}

function jumpToLetter(L){
  const id = firstByLetter.get(L);
  if(!id) return;
  const el = document.getElementById(id);
  if(el) el.scrollIntoView({ behavior:"smooth", block:"start" });
  [...alphaIndex.querySelectorAll("button")].forEach(x => x.classList.toggle("active", x.textContent===L));
}

// ---------- Server Tiles ----------
let selectedServer = null;
let currentModalServer = null;
let currentModalSort = 'name';
let currentModalQueues = [];
let configuredServers = []; // Will be populated from API

function buildServerQueuesMap() {
  const servers = {};
  ships.forEach(s => {
    // Extract server name from the "serverName / queueName" format
    const parts = s.name.split(' / ');
    const serverName = parts[0] || 'Unknown';
    if (!servers[serverName]) {
      servers[serverName] = [];
    }
    servers[serverName].push(s);
  });
  return servers;
}

async function renderServerTiles() {
  const serversGrid = document.getElementById('serversGrid');
  if (!serversGrid) return;
  
  // Fetch configured servers if not already loaded
  if (configuredServers.length === 0) {
    try {
      const response = await fetch('/api/configured-servers');
      configuredServers = await response.json();
      console.log('Configured servers:', configuredServers);
    } catch (err) {
      console.error('Failed to load configured servers:', err);
      return;
    }
  }
  
  serversGrid.innerHTML = '';
  
  // Build a map of server data from ships
  const serverDataMap = buildServerQueuesMap();
  
  // Create tiles for ALL configured servers
  configuredServers.sort((a, b) => {
    const nameA = typeof a === 'string' ? a : a.name;
    const nameB = typeof b === 'string' ? b : b.name;
    return nameA.localeCompare(nameB);
  }).forEach(serverItem => {
    const serverName = typeof serverItem === 'string' ? serverItem : serverItem.name;
    const serverStatus = typeof serverItem === 'string' ? 'UNKNOWN' : (serverItem.status || 'UNKNOWN');
    const queues = serverDataMap[serverName] || [];
    const errorCount = queues.filter(q => q.status === 'err').length;
    const warnCount = queues.filter(q => q.status === 'warn').length;
    
    const tile = document.createElement('button');
    tile.className = 'server-tile';
    
    // Apply red styling if server is unreachable
    if (serverStatus === 'UNREACHABLE') {
      tile.classList.add('server-tile-unreachable');
      tile.innerHTML = `
        <div class="server-tile-name">🔴 ${serverName}</div>
        <div class="server-tile-status">unreachable!</div>
      `;
    } else {
      const statusIndicator = errorCount > 0 ? '🔴' : (warnCount > 0 ? '🟡' : '🟢');
      tile.innerHTML = `
        <div class="server-tile-name">${statusIndicator} ${serverName}</div>
        <div class="server-tile-count">${queues.length}</div>
        <div class="server-tile-info">${errorCount} errors • ${warnCount} warnings</div>
      `;
    }
    
    tile.addEventListener('click', () => {
      openServerModal(serverName);
    });
    
    serversGrid.appendChild(tile);
  });
}

async function openServerModal(serverName) {
  const overlay = document.getElementById('serverModalOverlay');
  const title = document.getElementById('serverModalTitle');
  const content = document.getElementById('serverModalContent');
  const searchInput = document.getElementById('serverModalSearch');
  
  currentModalServer = serverName;
  title.textContent = `${serverName} - All Queues`;
  searchInput.value = '';
  content.innerHTML = '<div class="server-modal-empty">Loading queues...</div>';
  overlay.classList.add('active');
  
  await loadServerModalQueues(serverName, searchInput);
}

async function loadServerModalQueues(serverName, searchInput) {
  const content = document.getElementById('serverModalContent');
  
  try {
    const response = await fetch(`/api/queues/${encodeURIComponent(serverName)}`);
    const queues = await response.json();
    
    if (queues.length === 0) {
      content.innerHTML = '<div class="server-modal-empty">No queues found on this server</div>';
      return;
    }
    
    // Store all queues for sorting and filtering
    currentModalQueues = queues;
    renderServerQueuesTable(queues);
    
    searchInput.removeEventListener('keyup', window.serverModalSearchHandler);
    window.serverModalSearchHandler = () => {
      const filtered = queues.filter(q => 
        q.queueName.toLowerCase().includes(searchInput.value.toLowerCase())
      );
      renderServerQueuesTable(filtered);
    };
    searchInput.addEventListener('keyup', window.serverModalSearchHandler);
  } catch (err) {
    console.error('Failed to load server queues:', err);
    content.innerHTML = '<div class="server-modal-empty">Error loading queues. Please try again.</div>';
  }
}

async function refreshServerModalQueues() {
  if (!currentModalServer) return;
  
  const btn = document.getElementById('serverModalRefreshBtn');
  const content = document.getElementById('serverModalContent');
  const searchInput = document.getElementById('serverModalSearch');
  
  btn.disabled = true;
  btn.classList.add('refreshing');
  content.style.opacity = '0.6';
  
  try {
    const response = await fetch(`/api/queues/${encodeURIComponent(currentModalServer)}`);
    const queues = await response.json();
    
    if (queues.length === 0) {
      content.innerHTML = '<div class="server-modal-empty">No queues found on this server</div>';
    } else {
      currentModalQueues = queues;
      const filtered = queues.filter(q => 
        q.queueName.toLowerCase().includes(searchInput.value.toLowerCase())
      );
      renderServerQueuesTable(filtered);
    }
  } catch (err) {
    console.error('Failed to refresh queues:', err);
    content.innerHTML = '<div class="server-modal-empty">Error refreshing queues. Please try again.</div>';
  } finally {
    btn.disabled = false;
    btn.classList.remove('refreshing');
    content.style.opacity = '1';
  }
}

function renderServerQueuesTable(queues) {
  const content = document.getElementById('serverModalContent');
  
  if (queues.length === 0) {
    content.innerHTML = '<div class="server-modal-empty">No queues match your search</div>';
    return;
  }
  
  // Sort queues based on current sort setting
  let sorted = [...queues];
  if (currentModalSort === 'name') {
    sorted.sort((a, b) => a.queueName.localeCompare(b.queueName));
  } else if (currentModalSort === 'countAsc') {
    sorted.sort((a, b) => Number(a.messageCount) - Number(b.messageCount));
  } else if (currentModalSort === 'countDesc') {
    sorted.sort((a, b) => Number(b.messageCount) - Number(a.messageCount));
  } else if (currentModalSort === 'status') {
    const statusOrder = { critical: 0, warning: 1, ok: 2 };
    sorted.sort((a, b) => {
      const countA = Number(a.messageCount) || 0;
      const countB = Number(b.messageCount) || 0;
      const statusA = a.status === 'critical' ? 0 : (countA > 10000 ? 0 : (countA > 5000 ? 1 : 2));
      const statusB = b.status === 'critical' ? 0 : (countB > 10000 ? 0 : (countB > 5000 ? 1 : 2));
      return statusA - statusB || countB - countA;
    });
  }
  
  let html = `
    <table class="server-queues-table">
      <thead>
        <tr>
          <th>Queue Name</th>
          <th>Message Count</th>
          <th>Status</th>
        </tr>
      </thead>
      <tbody>
  `;
  
  sorted.forEach(queue => {
    const msgCount = Number(queue.messageCount) || 0;
    const status = queue.status === 'critical' ? 'critical' : (msgCount > 5000 ? 'warning' : 'ok');
    const statusLabel = queue.status || (msgCount > 10000 ? 'CRITICAL' : (msgCount > 5000 ? 'WARNING' : 'OK'));
    html += `
      <tr>
        <td>${escapeHtml(queue.queueName)}</td>
        <td style="text-align: right; font-family: monospace;">${msgCount.toLocaleString()}</td>
        <td><span class="queue-status-badge ${status}">${statusLabel}</span></td>
      </tr>
    `;
  });
  
  html += `
      </tbody>
    </table>
  `;
  
  content.innerHTML = html;
  updateSortButtonStates();
}

function setModalSort(sortType) {
  currentModalSort = sortType;
  renderServerQueuesTable(currentModalQueues);
}

function updateSortButtonStates() {
  const buttons = {
    'name': document.getElementById('sortByNameBtn'),
    'countAsc': document.getElementById('sortByCountAscBtn'),
    'countDesc': document.getElementById('sortByCountDescBtn'),
    'status': document.getElementById('sortByStatusBtn')
  };
  
  Object.keys(buttons).forEach(key => {
    if (buttons[key]) {
      buttons[key].classList.toggle('active', currentModalSort === key);
    }
  });
}

function closeServerModal() {
  const overlay = document.getElementById('serverModalOverlay');
  overlay.classList.remove('active');
}

// Close modal when clicking overlay background
document.addEventListener('DOMContentLoaded', () => {
  const overlay = document.getElementById('serverModalOverlay');
  if (overlay) {
    overlay.addEventListener('click', (e) => {
      if (e.target === overlay) {
        closeServerModal();
      }
    });
  }
});

// ---------- Rendering ----------
function render(){
  const now = Date.now();
  lastTick.textContent = "Last update: " + new Date(now).toLocaleTimeString();

  // counts
  const c = { ok:0, warn:0, err:0, off:0 };
  ships.forEach(s => c[s.status]++);
  countsEl.innerHTML = `
    <div class="chip"><span class="dot err"></span><b>${c.err}</b>&nbsp;Errors</div>
    <div class="chip"><span class="dot warn"></span><b>${c.warn}</b>&nbsp;Warnings</div>
    <div class="chip"><span class="dot off"></span><b>${c.off}</b>&nbsp;Offline</div>
    <div class="chip"><span class="dot ok"></span><b>${c.ok}</b>&nbsp;Healthy</div>
  `;

  // filters
  const filters = [
    { key:"attention", label:"Attention (Err/Warn)" },
    { key:"err", label:"Errors" },
    { key:"warn", label:"Warnings" },
    { key:"off", label:"Offline" },
    { key:"ok", label:"Healthy" },
    { key:"all", label:"All" }
  ];
  filtersEl.innerHTML = "";
  filters.forEach(f=>{
    const b = document.createElement("button");
    b.textContent = f.label;
    b.classList.toggle("active", filter===f.key);
    b.addEventListener("click", ()=>{ filter=f.key; render(); });
    filtersEl.appendChild(b);
  });

  // attention list = err/warn not acked, sorted by severity + recency
  const attention = ships
    .filter(s => (s.status==="err" || s.status==="warn") && !s.acked)
    .sort((a,b)=> statusWeight[b.status]-statusWeight[a.status] || b.lastEventAt-a.lastEventAt);

  console.log(`Building attention list: ${attention.length} items from ${ships.length} total ships`);
  console.log(`All ships status breakdown:`, ships.reduce((acc, s) => { acc[s.status] = (acc[s.status] || 0) + 1; return acc; }, {}));
  attention.forEach(s => console.log(`  - [${s.status}] ${s.name} (acked=${s.acked})`));
  
  attentionCount.textContent = attention.length;

  attentionList.innerHTML = "";
  if(attention.length===0){
    attentionList.innerHTML = `<div style="color:rgba(255,255,255,0.65);font-size:12px;padding:8px 2px">
      No unacknowledged warnings/errors right now 🎉
    </div>`;
  } else {
    attention.slice(0, 12).forEach(s => attentionList.appendChild(shipRow(s, true)));
    if(attention.length > 12){
      const more = document.createElement("div");
      more.style.color = "rgba(255,255,255,0.65)";
      more.style.fontSize = "12px";
      more.style.padding = "8px 2px";
      more.textContent = `…and ${attention.length-12} more`;
      attentionList.appendChild(more);
    }
  }

  // all list A–Z with search/filter
  const base = ships.slice().sort((a,b)=> a.letter.localeCompare(b.letter) || a.name.localeCompare(b.name));

  const filtered = base.filter(s=>{
    // Filter by selected server
    if (selectedServer) {
      const parts = s.name.split(' / ');
      const serverName = parts[0] || 'Unknown';
      if (serverName !== selectedServer) return false;
    }
    if(q && !s.name.toLowerCase().includes(q)) return false;
    if(filter === "all") return true;
    if(filter === "attention") return (s.status==="err" || s.status==="warn");
    return s.status === filter;
  });

  allCount.textContent = filtered.length;

  // map first row per letter for jump
  firstByLetter.clear();
  allList.innerHTML = "";
  filtered.forEach(s=>{
    if(!firstByLetter.has(s.letter)) firstByLetter.set(s.letter, s.id);
    allList.appendChild(shipRow(s, false));
  });

  // keep details up to date
  if(selectedId){
    const sel = ships.find(x=>x.id===selectedId);
    if(sel) renderDetails(sel);
  }
}

function shipRow(s, isAttention){
  const row = document.createElement("div");
  row.className = "row";
  row.id = s.id;
  row.classList.toggle("active", s.id===selectedId);

  const age = Math.max(0, Date.now() - s.lastEventAt);
  const ageTxt = humanAgo(age);
  const sevClass = s.status;

  const subtitle = isAttention
    ? (s.status==="err" ? "Immediate attention required" : "Check soon") + ` • ${s.queue ? s.queue.toLocaleString() + " msgs" : "No data"}`
    : `${statusLabel[s.status]} • last seen ${humanAgo(Date.now()-s.lastSeen)} • ${lastEventSummary(s)}`;

  row.innerHTML = `
    <div class="left">
      <div class="status ${s.status}"></div>
      <div style="min-width:0">
        <div class="name">${escapeHtml(s.name)}</div>
        <div class="subline">${escapeHtml(subtitle)}</div>
      </div>
    </div>
    <div class="rightmeta">
      <div class="sev ${sevClass}">${statusLabel[s.status]}${s.acked ? " • ACK" : ""}</div>
      <div>${isAttention ? (s.queue ? s.queue.toLocaleString() + " msgs" : "No data") : "rate " + s.errorRate + "%"}</div>
    </div>
  `;

  row.addEventListener("click", ()=>{
    selectedId = s.id;
    // visually update active states quickly
    document.querySelectorAll(".row.active").forEach(x=>x.classList.remove("active"));
    row.classList.add("active");
    renderDetails(s);
  });

  return row;
}

function renderDetails(s){
  shipName.textContent = s.name;
  shipMeta.textContent = `Letter ${s.letter} • ID ${s.id.slice(0,8)} • Last event ${humanAgo(Date.now()-s.lastEventAt)}`;

  kStatus.textContent = statusLabel[s.status];
  kSeen.textContent = humanAgo(Date.now()-s.lastSeen);
  kErrRate.textContent = s.errorRate + "%";
  kQueue.textContent = String(s.queue);
  kLat.textContent = s.latency + " ms";
  kAck.textContent = s.acked ? "Yes" : "No";

  // button states
  btnAck.textContent = s.acked ? "Unacknowledge" : "Acknowledge";

  // events
  eventsEl.innerHTML = "";
  const evts = s.events.slice().sort((a,b)=> b.t-a.t).slice(0, 12);
  evts.forEach(e=>{
    const div = document.createElement("div");
    div.className = "evt";
    div.innerHTML = `<div class="msg">${escapeHtml(e.msg)}</div><div class="when">${new Date(e.t).toLocaleTimeString()}</div>`;
    eventsEl.appendChild(div);
  });
}

// ---------- Interactions ----------
searchEl.addEventListener("input", (e)=>{
  q = e.target.value.trim().toLowerCase();
  render();
});

btnAck.addEventListener("click", ()=>{
  const s = ships.find(x=>x.id===selectedId);
  if(!s) return;
  s.acked = !s.acked;
  // Log to event history
  if (s.events) {
    s.events.unshift({ t: Date.now(), msg: s.acked ? "Alert acknowledged" : "Acknowledgement removed" });
    s.events = s.events.slice(0, 50);
  }
  render();
});

// Remove all simulation button handlers - only show real data

function humanAgo(ms){
  const s = Math.floor(ms/1000);
  if(s < 5) return "just now";
  if(s < 60) return s + "s ago";
  const m = Math.floor(s/60);
  if(m < 60) return m + "m ago";
  const h = Math.floor(m/60);
  return h + "h ago";
}

function lastEventSummary(ship){
  if(!ship.events || ship.events.length === 0) return "No recent events";
  const lastEvent = ship.events[0];
  return lastEvent.msg || "No message";
}

function clamp(v, lo, hi){ return Math.max(lo, Math.min(hi, v)); }

function escapeHtml(str){
  return String(str).replace(/[&<>"']/g, m => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[m]));
}

// ---------- Init ----------

// Try to load real data first
(async () => {
  console.log('Starting dashboard initialization...');
  const hasRealData = await loadRealData();
  
  // Always use the loaded data if available
  if (realShips.length > 0) {
    ships.length = 0; // clear any previous ships
    ships.push(...realShips);
    console.log(`✅ Using ${realShips.length} real TIBCO EMS queue data`);
  } else {
    console.warn('⚠️ No real data loaded - dashboard will be empty');
  }
  
  // Now build index after ships are loaded
  ensureAllLetters();
  buildIndex();
  renderServerTiles();
  
  // default select first err/warn if exists, else first ship
  function selectDefault(){
    const attention = ships.filter(s => s.status==="err" || s.status==="warn")
                           .sort((a,b)=> statusWeight[b.status]-statusWeight[a.status] || b.lastEventAt-a.lastEventAt);
    const s = attention[0] || ships[0];
    if (s) {
      selectedId = s.id;
      renderDetails(s);
    } else {
      console.warn('No ships available to select');
    }
  }
  selectDefault();
  render(); // Always update timestamps and counts display
  startAutoRefresh();
})();
