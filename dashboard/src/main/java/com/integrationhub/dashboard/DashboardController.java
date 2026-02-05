package com.integrationhub.dashboard;

import com.integrationhub.dashboard.config.TibcoEmsProperties;
import com.integrationhub.dashboard.model.QueueInfo;
import com.integrationhub.dashboard.service.TibcoEmsQueueService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {

    private final TibcoEmsQueueService tibcoEmsQueueService;

    public DashboardController(TibcoEmsQueueService tibcoEmsQueueService) {
        this.tibcoEmsQueueService = tibcoEmsQueueService;
    }

    @GetMapping("/dashboard")
    @ResponseBody
    public String dashboard() {
        return """
                <!doctype html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8" />
                  <meta name="viewport" content="width=device-width, initial-scale=1"/>
                  <title>EMS Health Dashboard </title>
                  <style>
                    :root{
                      --bg0:#070b18; --bg1:#0b1020;
                      --panel: rgba(255,255,255,0.06);
                      --panel2: rgba(255,255,255,0.08);
                      --border: rgba(255,255,255,0.12);
                      --text: rgba(255,255,255,0.92);
                      --muted: rgba(255,255,255,0.65);

                      --ok:#23c483;
                      --warn:#ffc107;
                      --err:#ff5757;
                      --off:#8aa0b5;

                      --accent:#7c5cff;
                      --shadow: 0 10px 30px rgba(0,0,0,0.35);
                      --radius: 16px;
                      --gap: 14px;

                      --indexW: 42px;
                    }

                    *{ box-sizing:border-box; }
                    body{
                      margin:0;
                      font-family: ui-sans-serif, system-ui, -apple-system, Segoe UI, Roboto, Helvetica, Arial;
                      color: var(--text);
                      background:
                        radial-gradient(1100px 900px at 15% 12%, rgba(124,92,255,0.33), transparent 60%),
                        radial-gradient(900px 700px at 88% 25%, rgba(35,196,131,0.22), transparent 55%),
                        linear-gradient(180deg, var(--bg0), var(--bg1) 60%, var(--bg0));
                      min-height:100vh;
                    }

                    header{
                      position:sticky; top:0; z-index:30;
                      background: rgba(7,11,24,0.75);
                      backdrop-filter: blur(10px);
                      border-bottom: 1px solid var(--border);
                      box-shadow: 0 8px 22px rgba(0,0,0,0.22);
                    }
                    .topbar{
                      display:flex; align-items:center; justify-content:space-between;
                      padding: 14px 18px; gap:12px;
                    }
                    .title{
                      display:flex; align-items:center; gap:10px;
                      font-weight:900; letter-spacing:.3px;
                    }
                    .badge{
                      font-size:12px;
                      padding:6px 10px;
                      border-radius:999px;
                      background: linear-gradient(90deg, rgba(124,92,255,0.9), rgba(35,196,131,0.75));
                      box-shadow: 0 10px 24px rgba(0,0,0,0.25);
                    }
                    .hint{
                      font-size:12px; color:var(--muted);
                      white-space:nowrap; overflow:hidden; text-overflow:ellipsis;
                      max-width: 60%;
                    }
                    .back-btn{
                      all:unset; cursor:pointer;
                      padding: 8px 16px;
                      border-radius: 10px;
                      border: 1px solid rgba(255,255,255,0.15);
                      background: rgba(255,255,255,0.06);
                      font-size:13px;
                      color: rgba(255,255,255,0.85);
                      transition: all 150ms ease;
                      display:flex; align-items:center; gap:6px;
                      white-space:nowrap;
                    }
                    .back-btn:hover{
                      background: rgba(124,92,255,0.15);
                      border-color: rgba(124,92,255,0.30);
                      transform: translateY(-1px);
                      box-shadow: 0 4px 12px rgba(0,0,0,0.20);
                    }
                    .back-btn:active{
                      transform: translateY(0px);
                    }
                    .refresh-btn{
                      all:unset; cursor:pointer;
                      padding: 8px 16px;
                      border-radius: 10px;
                      border: 1px solid rgba(35,196,131,0.20);
                      background: rgba(35,196,131,0.08);
                      font-size:13px;
                      color: rgba(35,196,131,0.95);
                      transition: all 150ms ease;
                      display:flex; align-items:center; gap:6px;
                      white-space:nowrap;
                      margin-right: 8px;
                    }
                    .refresh-btn:hover{
                      background: rgba(35,196,131,0.15);
                      border-color: rgba(35,196,131,0.35);
                      transform: translateY(-1px);
                      box-shadow: 0 4px 12px rgba(35,196,131,0.25);
                    }
                    .refresh-btn:active{
                      transform: translateY(0px);
                    }
                    .refresh-btn.refreshing{
                      pointer-events: none;
                      opacity: 0.6;
                    }
                    .refresh-btn.refreshing svg{
                      animation: spin 1s linear infinite;
                    }
                    @keyframes spin{
                      from{ transform: rotate(0deg); }
                      to{ transform: rotate(360deg); }
                    }

                    main{ padding:18px; }
                    .layout{
                      display:grid;
                      grid-template-columns: 1fr;
                      grid-template-rows: auto 1fr;
                      gap: var(--gap);
                      min-height: calc(100vh - 70px);
                    }
                    .layout > .panel:first-child{
                      grid-column: 1;
                      grid-row: 1;
                    }
                    .layout > .panel:last-child{
                      display: none;
                    }
                    .bottom-panels{
                      display: grid;
                      grid-template-columns: 1.1fr 0.9fr;
                      gap: var(--gap);
                      grid-column: 1;
                      grid-row: 2;
                    }

                    .panel{
                      background: var(--panel);
                      border:1px solid var(--border);
                      border-radius: var(--radius);
                      box-shadow: var(--shadow);
                      overflow:hidden;
                      position:relative;
                    }
                    .panel-head{
                      display:flex; align-items:center; justify-content:space-between;
                      padding: 12px 14px;
                      border-bottom: 1px solid var(--border);
                      background: rgba(255,255,255,0.03);
                    }
                    .panel-head h2{
                      margin:0;
                      font-size: 13px;
                      text-transform:uppercase;
                      letter-spacing:.35px;
                      color: rgba(255,255,255,0.85);
                    }
                    .panel-head .sub{
                      font-size:12px; color:var(--muted);
                    }

                    /* LEFT: index + list */
                    .left-wrap{
                      display:grid;
                      grid-template-columns: var(--indexW) 1fr;
                      height: calc(100vh - 70px - 36px);
                      min-height: 560px;
                    }
                    .alpha-index{
                      border-right:1px solid var(--border);
                      background: rgba(0,0,0,0.12);
                      padding:10px 6px;
                      display:flex; flex-direction:column; gap:6px;
                      user-select:none;
                    }
                    .alpha-index button{
                      all:unset;
                      cursor:pointer;
                      text-align:center;
                      font-size:11px;
                      padding:6px 0;
                      border-radius:10px;
                      color: rgba(255,255,255,0.72);
                      transition: transform 120ms ease, background 120ms ease, color 120ms ease;
                    }
                    .alpha-index button:hover{
                      background: rgba(124,92,255,0.18);
                      color: rgba(255,255,255,0.95);
                      transform: translateY(-1px);
                    }
                    .alpha-index button.active{
                      background: rgba(124,92,255,0.28);
                      color: rgba(255,255,255,0.95);
                      outline: 1px solid rgba(124,92,255,0.35);
                    }

                    .scroll-area{
                      overflow:auto;
                      height:100%;
                      padding: 12px;
                      scroll-behavior:smooth;
                      position:relative;
                    }

                    .sticky-top{
                      position: sticky;
                      top: 0;
                      z-index: 5;
                      margin: -12px -12px 12px -12px;
                      padding: 12px;
                      background: rgba(7,11,24,0.60);
                      backdrop-filter: blur(10px);
                      border-bottom: 1px solid var(--border);
                    }

                    .summary{
                      display:flex;
                      gap:10px;
                      flex-wrap:wrap;
                      align-items:center;
                      justify-content: space-between;
                      margin-bottom:10px;
                    }
                    .counts{
                      display:flex; gap:8px; flex-wrap:wrap; align-items:center;
                    }
                    .chip{
                      display:flex; align-items:center; gap:8px;
                      padding: 6px 10px;
                      border-radius: 999px;
                      border: 1px solid rgba(255,255,255,0.10);
                      background: rgba(255,255,255,0.06);
                      font-size: 12px;
                      color: rgba(255,255,255,0.86);
                      user-select:none;
                    }
                    .dot{ width:8px; height:8px; border-radius:999px; }
                    .dot.ok{ background: var(--ok); }
                    .dot.warn{ background: var(--warn); }
                    .dot.err{ background: var(--err); }
                    .dot.off{ background: var(--off); }

                    .filters{
                      display:flex; gap:8px; flex-wrap:wrap; align-items:center;
                    }
                    .filters button{
                      all:unset; cursor:pointer;
                      padding:6px 10px;
                      border-radius: 999px;
                      border: 1px solid rgba(255,255,255,0.10);
                      background: rgba(255,255,255,0.05);
                      font-size:12px;
                      color: rgba(255,255,255,0.78);
                      transition: background 120ms ease, border 120ms ease, transform 120ms ease;
                    }
                    .filters button:hover{
                      background: rgba(124,92,255,0.12);
                      border-color: rgba(124,92,255,0.25);
                      transform: translateY(-1px);
                    }
                    .filters button.active{
                      background: rgba(124,92,255,0.22);
                      border-color: rgba(124,92,255,0.35);
                      color: rgba(255,255,255,0.92);
                    }
                    .search{
                      display:flex; gap:10px; align-items:center;
                      margin-top: 8px;
                    }
                    .search input{
                      width:100%;
                      padding: 10px 12px;
                      border-radius: 12px;
                      border: 1px solid rgba(255,255,255,0.12);
                      background: rgba(0,0,0,0.20);
                      color: var(--text);
                      outline:none;
                    }
                    .search input::placeholder{ color: rgba(255,255,255,0.45); }

                    .section-title{
                      display:flex; align-items:center; justify-content:space-between;
                      margin: 14px 0 10px;
                      color: rgba(255,255,255,0.86);
                      font-size: 12px;
                      text-transform: uppercase;
                      letter-spacing:.35px;
                    }
                    .section-title .pill{
                      font-size: 11px;
                      padding: 4px 8px;
                      border-radius: 999px;
                      background: rgba(255,255,255,0.06);
                      border: 1px solid rgba(255,255,255,0.10);
                      color: rgba(255,255,255,0.78);
                    }

                    .attention-list{
                      display:grid;
                      grid-template-columns: 1fr;
                      gap: 8px;
                    }

                    .row{
                      display:flex;
                      align-items:center;
                      justify-content:space-between;
                      gap:10px;
                      padding: 10px 10px;
                      border-radius: 14px;
                      border: 1px solid rgba(255,255,255,0.10);
                      background: linear-gradient(180deg, rgba(255,255,255,0.07), rgba(255,255,255,0.04));
                      box-shadow: 0 10px 20px rgba(0,0,0,0.18);
                      cursor:pointer;
                      transition: transform 130ms ease, border 130ms ease, background 130ms ease;
                    }
                    .row:hover{
                      transform: translateY(-1px);
                      border-color: rgba(124,92,255,0.35);
                      background: linear-gradient(180deg, rgba(124,92,255,0.14), rgba(255,255,255,0.04));
                    }
                    .row.active{
                      border-color: rgba(124,92,255,0.55);
                      outline: 1px solid rgba(124,92,255,0.22);
                    }
                    .left{
                      display:flex; align-items:center; gap:10px; min-width:0;
                    }
                    .status{
                      width:10px; height:10px; border-radius:999px;
                      box-shadow: 0 0 0 3px rgba(0,0,0,0.18);
                    }
                    .status.ok{ background: var(--ok); }
                    .status.warn{ background: var(--warn); }
                    .status.err{ background: var(--err); }
                    .status.off{ background: var(--off); }

                    .name{
                      font-weight: 800;
                      font-size: 13px;
                      white-space:nowrap; overflow:hidden; text-overflow:ellipsis;
                    }
                    .subline{
                      font-size: 11px;
                      color: var(--muted);
                      white-space:nowrap; overflow:hidden; text-overflow:ellipsis;
                      margin-top: 2px;
                    }
                    .rightmeta{
                      text-align:right;
                      min-width: 130px;
                      font-size: 11px;
                      color: rgba(255,255,255,0.70);
                    }
                    .sev{
                      display:inline-block;
                      padding: 4px 8px;
                      border-radius: 999px;
                      font-size: 11px;
                      border: 1px solid rgba(255,255,255,0.10);
                      background: rgba(255,255,255,0.06);
                      margin-bottom: 6px;
                      white-space:nowrap;
                    }
                    .sev.err{ border-color: rgba(255,87,87,0.35); background: rgba(255,87,87,0.12); }
                    .sev.warn{ border-color: rgba(255,193,7,0.35); background: rgba(255,193,7,0.10); }
                    .sev.ok{ border-color: rgba(35,196,131,0.35); background: rgba(35,196,131,0.10); }
                    .sev.off{ border-color: rgba(138,160,181,0.35); background: rgba(138,160,181,0.10); }

                    /* RIGHT: details */
                    .dash{
                      padding: 14px;
                      height: calc(100vh - 70px - 36px);
                      min-height: 560px;
                      overflow:auto;
                      display:grid;
                      grid-template-rows: auto auto 1fr;
                      gap: 12px;
                    }
                    .hero{
                      background: var(--panel2);
                      border: 1px solid rgba(255,255,255,0.12);
                      border-radius: 14px;
                      padding: 14px;
                      box-shadow: 0 10px 22px rgba(0,0,0,0.22);
                    }
                    .hero-top{
                      display:flex; align-items:center; justify-content:space-between; gap:12px;
                      margin-bottom: 10px;
                    }
                    .hero h3{
                      margin:0;
                      font-size: 14px;
                      text-transform: uppercase;
                      letter-spacing: .35px;
                      color: rgba(255,255,255,0.85);
                    }
                    .hero .shipname{
                      font-size: 18px;
                      font-weight: 950;
                      letter-spacing: .2px;
                      margin: 6px 0 2px;
                    }
                    .hero .meta{
                      font-size: 12px;
                      color: var(--muted);
                    }
                    .actions{
                      display:flex; gap:8px; flex-wrap:wrap; justify-content:flex-end;
                    }
                    .btn{
                      all:unset;
                      cursor:pointer;
                      padding: 8px 10px;
                      border-radius: 12px;
                      border: 1px solid rgba(255,255,255,0.12);
                      background: rgba(255,255,255,0.06);
                      font-size: 12px;
                      color: rgba(255,255,255,0.84);
                      transition: transform 120ms ease, background 120ms ease, border 120ms ease;
                      user-select:none;
                    }
                    .btn:hover{ transform: translateY(-1px); background: rgba(124,92,255,0.14); border-color: rgba(124,92,255,0.25); }
                    .btn.primary{ background: rgba(35,196,131,0.12); border-color: rgba(35,196,131,0.25); }
                    .btn.danger{ background: rgba(255,87,87,0.12); border-color: rgba(255,87,87,0.25); }

                    .grid2{
                      display:grid;
                      grid-template-columns: repeat(3, minmax(0, 1fr));
                      gap:10px;
                      margin-top: 12px;
                    }
                    .kpi{
                      background: rgba(0,0,0,0.18);
                      border: 1px solid rgba(255,255,255,0.10);
                      border-radius: 14px;
                      padding: 12px;
                    }
                    .kpi .label{ font-size: 12px; color: var(--muted); }
                    .kpi .val{ font-size: 18px; font-weight: 950; margin-top: 8px; }

                    .log{
                      background: var(--panel2);
                      border: 1px solid rgba(255,255,255,0.12);
                      border-radius: 14px;
                      padding: 12px;
                      box-shadow: 0 10px 22px rgba(0,0,0,0.22);
                      overflow:hidden;
                    }
                    .log h3{
                      margin:0 0 10px 0;
                      font-size: 13px;
                      text-transform: uppercase;
                      letter-spacing: .35px;
                      color: rgba(255,255,255,0.85);
                    }
                    .events{
                      display:flex;
                      flex-direction:column;
                      gap:8px;
                      max-height: 340px;
                      overflow:auto;
                      padding-right: 4px;
                    }
                    .evt{
                      padding: 10px;
                      border-radius: 14px;
                      border: 1px solid rgba(255,255,255,0.10);
                      background: rgba(255,255,255,0.05);
                      font-size: 12px;
                      color: rgba(255,255,255,0.86);
                      display:flex;
                      justify-content:space-between;
                      gap:12px;
                    }
                    .evt .when{ color: rgba(255,255,255,0.60); font-size: 11px; white-space:nowrap; }
                    .evt .msg{ min-width:0; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }

                    /* SERVER TILES */
                    .servers-section{
                      padding: 18px;
                      border-bottom: 1px solid var(--border);
                      background: linear-gradient(180deg, rgba(255,255,255,0.03), rgba(255,255,255,0.01));
                    }
                    .servers-section h3{
                      margin: 0 0 14px 0;
                      font-size: 13px;
                      text-transform: uppercase;
                      letter-spacing: .35px;
                      color: rgba(255,255,255,0.85);
                      font-weight: 700;
                    }
                    .servers-grid{
                      display: grid;
                      grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
                      gap: 12px;
                    }
                    .server-tile{
                      all: unset;
                      cursor: pointer;
                      background: rgba(255,255,255,0.06);
                      border: 1px solid rgba(255,255,255,0.12);
                      border-radius: 12px;
                      padding: 14px;
                      transition: all 200ms ease;
                      display: flex;
                      flex-direction: column;
                      gap: 10px;
                    }
                    .server-tile:hover{
                      background: rgba(124,92,255,0.12);
                      border-color: rgba(124,92,255,0.25);
                      transform: translateY(-2px);
                      box-shadow: 0 8px 20px rgba(124,92,255,0.15);
                    }
                    .server-tile.active{
                      background: rgba(35,196,131,0.15);
                      border-color: rgba(35,196,131,0.35);
                      box-shadow: 0 10px 25px rgba(35,196,131,0.20);
                    }
                    .server-tile-name{
                      font-size: 14px;
                      font-weight: 800;
                      color: rgba(255,255,255,0.92);
                      white-space: nowrap;
                      overflow: hidden;
                      text-overflow: ellipsis;
                    }
                    .server-tile-info{
                      font-size: 11px;
                      color: rgba(255,255,255,0.65);
                    }
                    .server-tile-count{
                      font-size: 16px;
                      font-weight: 900;
                      color: rgba(35,196,131,0.92);
                      margin-top: 4px;
                    }
                    .server-tile-unreachable{
                      background: rgba(239,68,68,0.12) !important;
                      border-color: rgba(239,68,68,0.35) !important;
                    }
                    .server-tile-unreachable:hover{
                      background: rgba(239,68,68,0.18) !important;
                      border-color: rgba(239,68,68,0.45) !important;
                      box-shadow: 0 8px 20px rgba(239,68,68,0.15) !important;
                    }
                    .server-tile-status{
                      font-size: 12px;
                      font-weight: 700;
                      color: rgba(239,68,68,0.95);
                      text-transform: uppercase;
                      letter-spacing: 0.5px;
                    }

                    /* SERVER DETAIL MODAL */
                    .server-modal-overlay{
                      display: none;
                      position: fixed;
                      top: 0;
                      left: 0;
                      right: 0;
                      bottom: 0;
                      background: rgba(0,0,0,0.65);
                      backdrop-filter: blur(3px);
                      z-index: 100;
                      animation: fadeIn 200ms ease;
                    }
                    .server-modal-overlay.active{
                      display: flex;
                      align-items: center;
                      justify-content: center;
                    }
                    @keyframes fadeIn {
                      from { opacity: 0; }
                      to { opacity: 1; }
                    }
                    .server-modal{
                      background: linear-gradient(135deg, rgba(11,16,32,0.98), rgba(7,11,24,0.98));
                      border: 1px solid rgba(255,255,255,0.15);
                      border-radius: 20px;
                      box-shadow: 0 25px 50px rgba(0,0,0,0.5);
                      width: 90%;
                      max-width: 900px;
                      max-height: 85vh;
                      display: flex;
                      flex-direction: column;
                      animation: slideUp 300ms ease;
                    }
                    @keyframes slideUp {
                      from { transform: translateY(30px); opacity: 0; }
                      to { transform: translateY(0); opacity: 1; }
                    }
                    .server-modal-header{
                      padding: 20px;
                      border-bottom: 1px solid rgba(255,255,255,0.12);
                      display: flex;
                      justify-content: space-between;
                      align-items: center;
                    }
                    .server-modal-header h2{
                      margin: 0;
                      font-size: 18px;
                      font-weight: 900;
                      letter-spacing: 0.3px;
                    }
                    .server-modal-close{
                      all: unset;
                      cursor: pointer;
                      padding: 8px;
                      border-radius: 8px;
                      background: rgba(255,255,255,0.08);
                      border: 1px solid rgba(255,255,255,0.12);
                      color: rgba(255,255,255,0.85);
                      font-size: 20px;
                      display: flex;
                      align-items: center;
                      justify-content: center;
                      transition: all 150ms ease;
                    }
                    .server-modal-close:hover{
                      background: rgba(255,87,87,0.15);
                      border-color: rgba(255,87,87,0.35);
                    }
                    #serverModalRefreshBtn{
                      display: flex;
                      align-items: center;
                      gap: 6px;
                    }
                    #serverModalRefreshBtn:disabled{
                      opacity: 0.5;
                      cursor: not-allowed;
                    }
                    #serverModalRefreshBtn.refreshing svg{
                      animation: spin 800ms linear infinite;
                    }
                    @keyframes spin {
                      from { transform: rotate(0deg); }
                      to { transform: rotate(360deg); }
                    }
                      transition: all 150ms ease;
                    }
                    .server-modal-close:hover{
                      background: rgba(255,87,87,0.15);
                      border-color: rgba(255,87,87,0.35);
                    }
                    .server-modal-search{
                      padding: 14px 20px;
                      border-bottom: 1px solid rgba(255,255,255,0.10);
                    }
                    .server-modal-sorts{
                      display: flex;
                      gap: 6px;
                    }
                    .sort-btn{
                      all: unset;
                      cursor: pointer;
                      padding: 8px 12px;
                      border-radius: 8px;
                      border: 1px solid rgba(255,255,255,0.15);
                      background: rgba(255,255,255,0.06);
                      color: rgba(255,255,255,0.75);
                      font-size: 11px;
                      font-weight: 600;
                      white-space: nowrap;
                      transition: all 150ms ease;
                    }
                    .sort-btn:hover{
                      background: rgba(124,92,255,0.12);
                      border-color: rgba(124,92,255,0.25);
                    }
                    .sort-btn.active{
                      background: rgba(35,196,131,0.15);
                      border-color: rgba(35,196,131,0.35);
                      color: rgba(35,196,131,0.95);
                    }
                    .server-modal-search input{
                      all: unset;
                      width: 100%;
                      padding: 10px 12px;
                      border-radius: 10px;
                      border: 1px solid rgba(255,255,255,0.12);
                      background: rgba(255,255,255,0.05);
                      color: rgba(255,255,255,0.92);
                      font-size: 13px;
                    }
                    .server-modal-search input::placeholder{
                      color: rgba(255,255,255,0.50);
                    }
                    .server-modal-search input:focus{
                      outline: none;
                      background: rgba(255,255,255,0.08);
                      border-color: rgba(124,92,255,0.35);
                    }
                    .server-modal-content{
                      flex: 1;
                      overflow-y: auto;
                      padding: 0;
                    }
                    .server-queues-table{
                      width: 100%;
                      border-collapse: collapse;
                    }
                    .server-queues-table thead{
                      position: sticky;
                      top: 0;
                      background: rgba(255,255,255,0.06);
                      border-bottom: 1px solid rgba(255,255,255,0.12);
                    }
                    .server-queues-table th{
                      padding: 12px 14px;
                      text-align: left;
                      font-size: 12px;
                      font-weight: 700;
                      color: rgba(255,255,255,0.75);
                      text-transform: uppercase;
                      letter-spacing: 0.5px;
                      border-right: 1px solid rgba(255,255,255,0.08);
                    }
                    .server-queues-table th:last-child{
                      border-right: none;
                    }
                    .server-queues-table td{
                      padding: 12px 14px;
                      font-size: 12px;
                      border-bottom: 1px solid rgba(255,255,255,0.06);
                      border-right: 1px solid rgba(255,255,255,0.08);
                      color: rgba(255,255,255,0.85);
                    }
                    .server-queues-table td:last-child{
                      border-right: none;
                    }
                    .server-queues-table tbody tr:hover{
                      background: rgba(124,92,255,0.08);
                    }
                    .queue-status-badge{
                      display: inline-block;
                      padding: 4px 8px;
                      border-radius: 6px;
                      font-weight: 600;
                      font-size: 11px;
                    }
                    .queue-status-badge.critical{
                      background: rgba(255,87,87,0.20);
                      color: rgba(255,99,99,0.95);
                      border: 1px solid rgba(255,87,87,0.35);
                    }
                    .queue-status-badge.warning{
                      background: rgba(255,193,7,0.15);
                      color: rgba(255,193,7,0.95);
                      border: 1px solid rgba(255,193,7,0.35);
                    }
                    .queue-status-badge.ok{
                      background: rgba(35,196,131,0.15);
                      color: rgba(35,196,131,0.95);
                      border: 1px solid rgba(35,196,131,0.35);
                    }
                    .server-modal-empty{
                      padding: 40px 20px;
                      text-align: center;
                      color: rgba(255,255,255,0.60);
                      font-size: 13px;
                    }

                    @media (max-width: 980px){
                      .layout{ grid-template-columns: 1fr; }
                      .left-wrap, .dash{ height:auto; min-height: 560px; }
                      .grid2{ grid-template-columns: repeat(2, minmax(0,1fr)); }
                      .servers-grid{ grid-template-columns: repeat(auto-fit, minmax(140px, 1fr)); }
                      .server-modal{ width: 95%; max-height: 90vh; }
                    }
                  </style>
                </head>
                <body>
                <header>
                  <div class="topbar">
                    <div class="title"><span class="badge">Operations</span> EMS Health Dashboard</div>
                    <div class="hint">Attention surfaces automatically • Filter by status • Click a ship → details & events • <span id="nextRefresh">Next refresh in 2:00</span></div>
                    <div style="display:flex; gap:8px;">
                      <button class="refresh-btn" id="refreshBtn" onclick="refreshData()">
                        <svg width="16" height="16" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="2">
                          <path d="M14 8c0-3.3-2.7-6-6-6-2.4 0-4.4 1.4-5.4 3.4M2 8c0 3.3 2.7 6 6 6 2.4 0 4.4-1.4 5.4-3.4M14 4v4h-4M2 12v-4h4"/>
                        </svg>
                        Refresh Now
                      </button>
                      <button class="back-btn" onclick="window.location.href='/'">
                        <svg width="16" height="16" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="2">
                          <path d="M10 12L6 8l4-4"/>
                        </svg>
                        Back to Home
                      </button>
                    </div>
                  </div>
                </header>

                <main>
                  <section class="layout">

                    <!-- FLEET PANEL - FULL WIDTH -->
                    <div class="panel">
                      <div class="panel-head">
                        <h2>Fleet</h2>
                        <div class="sub"><span id="lastTick">—</span></div>
                      </div>

                      <!-- SERVER TILES SECTION -->
                      <div class="servers-section">
                        <h3>EMS Servers</h3>
                        <div class="servers-grid" id="serversGrid"></div>
                      </div>
                    </div>

                    <!-- BOTTOM PANELS - NEEDS ATTENTION & SHIP DETAILS -->
                    <div class="bottom-panels">
                      <!-- LEFT: NEEDS ATTENTION & ALL SHIPS -->
                      <div class="panel">
                        <div class="panel-head">
                          <h2>Ship List</h2>
                          <div class="sub">Select a ship for details</div>
                        </div>

                        <div class="left-wrap">
                          <div class="alpha-index" id="alphaIndex"></div>

                          <div class="scroll-area" id="scrollArea">
                            <div class="sticky-top">
                              <div class="summary">
                                <div class="counts" id="counts"></div>
                                <div class="filters" id="filters"></div>
                              </div>
                              <div class="search">
                                <input id="search" placeholder="Search ship name (e.g. Apex, Icon)..." />
                              </div>
                            </div>

                            <div class="section-title">
                              <span>Needs Attention</span>
                              <span class="pill" id="attentionCount">0</span>
                            </div>
                            <div class="attention-list" id="attentionList"></div>

                            <div class="section-title" style="margin-top:18px;">
                              <span>All Ships (A–Z)</span>
                              <span class="pill" id="allCount">0</span>
                            </div>
                            <div class="attention-list" id="allList"></div>
                          </div>
                        </div>
                      </div>

                      <!-- RIGHT: SHIP DETAILS -->
                      <div class="panel">
                        <div class="panel-head">
                          <h2>Ship Details</h2>
                          <div class="sub">status, KPIs, recent events</div>
                        </div>

                        <div class="dash">
                          <div class="hero" id="hero">
                            <div class="hero-top">
                              <div>
                                <h3>Selected Ship</h3>
                                <div class="shipname" id="shipName">—</div>
                                <div class="meta" id="shipMeta">Click a ship on the left</div>
                              </div>
                              <div class="actions">
                                <button class="btn" id="btnAck">Acknowledge</button>
                              </div>
                            </div>

                            <div class="grid2">
                              <div class="kpi"><div class="label">Status</div><div class="val" id="kStatus">—</div></div>
                              <div class="kpi"><div class="label">Last Seen</div><div class="val" id="kSeen">—</div></div>
                              <div class="kpi"><div class="label">Error Rate</div><div class="val" id="kErrRate">—</div></div>
                              <div class="kpi"><div class="label">Queue Depth</div><div class="val" id="kQueue">—</div></div>
                              <div class="kpi"><div class="label">Latency</div><div class="val" id="kLat">—</div></div>
                              <div class="kpi"><div class="label">Acked?</div><div class="val" id="kAck">—</div></div>
                            </div>
                          </div>

                          <div class="log">
                            <h3>Recent Events</h3>
                            <div class="events" id="events"></div>
                          </div>

                          <div class="log">
                            <h3>Notes</h3>
                            <div style="color:rgba(255,255,255,0.70);font-size:12px;line-height:1.5">
                              Dashboard shows the key idea: <b>operators don't hunt</b> — the UI
                              elevates ships with errors/warnings automatically while still offering A–Z lookup.
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>

                  </section>
                </main>

                <!-- SERVER DETAIL MODAL -->
                <div class="server-modal-overlay" id="serverModalOverlay">
                  <div class="server-modal" id="serverModal">
                    <div class="server-modal-header">
                      <h2 id="serverModalTitle">Server Queues</h2>
                      <div style="display: flex; gap: 8px;">
                        <button class="btn" id="serverModalRefreshBtn" onclick="refreshServerModalQueues()" style="font-size: 12px;">
                          <svg width="14" height="14" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="2">
                            <path d="M14 8c0-3.3-2.7-6-6-6-2.4 0-4.4 1.4-5.4 3.4M2 8c0 3.3 2.7 6 6 6 2.4 0 4.4-1.4 5.4-3.4M14 4v4h-4M2 12v-4h4"/>
                          </svg>
                          Refresh
                        </button>
                        <button class="server-modal-close" onclick="closeServerModal()">&times;</button>
                      </div>
                    </div>
                    <div class="server-modal-search">
                      <div style="display: flex; gap: 10px; align-items: center; margin-bottom: 10px;">
                        <input type="text" id="serverModalSearch" placeholder="Search queue names..." style="flex: 1;" />
                        <div class="server-modal-sorts">
                          <button class="sort-btn" id="sortByNameBtn" onclick="setModalSort('name')" title="Sort by Name A-Z">Name</button>
                          <button class="sort-btn" id="sortByCountAscBtn" onclick="setModalSort('countAsc')" title="Sort by Count ↑">Count ↑</button>
                          <button class="sort-btn" id="sortByCountDescBtn" onclick="setModalSort('countDesc')" title="Sort by Count ↓">Count ↓</button>
                          <button class="sort-btn" id="sortByStatusBtn" onclick="setModalSort('status')" title="Sort by Status">Status</button>
                        </div>
                      </div>
                    </div>
                    <div class="server-modal-content" id="serverModalContent">
                      <div class="server-modal-empty">Loading queues...</div>
                    </div>
                  </div>
                </div>

                <script>
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
                        let shipStatus = 'ok';
                        if (queue.status === 'critical' || queue.messageCount > 10000) {
                          shipStatus = 'err';
                        } else if (queue.status === 'warning' || queue.messageCount > 5000) {
                          shipStatus = 'warn';
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
                      sorted.sort((a, b) => a.messageCount - b.messageCount);
                    } else if (currentModalSort === 'countDesc') {
                      sorted.sort((a, b) => b.messageCount - a.messageCount);
                    } else if (currentModalSort === 'status') {
                      const statusOrder = { critical: 0, warning: 1, ok: 2 };
                      sorted.sort((a, b) => {
                        const statusA = a.status === 'critical' ? 0 : (a.messageCount > 10000 ? 0 : (a.messageCount > 5000 ? 1 : 2));
                        const statusB = b.status === 'critical' ? 0 : (b.messageCount > 10000 ? 0 : (b.messageCount > 5000 ? 1 : 2));
                        return statusA - statusB || b.messageCount - a.messageCount;
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
                      const status = queue.status === 'critical' ? 'critical' : (queue.messageCount > 5000 ? 'warning' : 'ok');
                      const statusLabel = queue.status || (queue.messageCount > 10000 ? 'CRITICAL' : (queue.messageCount > 5000 ? 'WARNING' : 'OK'));
                      html += `
                        <tr>
                          <td>${escapeHtml(queue.queueName)}</td>
                          <td style="text-align: right; font-family: monospace;">${queue.messageCount.toLocaleString()}</td>
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
                    attention.forEach(s => console.log(`  - [${s.status}] ${s.name}`));
                    
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
                        render();
                        renderDetails(s);
                      } else {
                        console.warn('No ships available to select');
                      }
                    }
                    selectDefault();
                    startAutoRefresh();
                  })();
                </script>
                </body>
                </html>
                """;
    }

    @GetMapping("/api/queues")
    @ResponseBody
    public List<QueueInfo> getQueuesApi() {
        return tibcoEmsQueueService.getHighVolumeQueues();
    }

    @GetMapping("/api/status")
    @ResponseBody
    public String getStatus() {
        return """
                {
                  "tibcoLibAvailable": %s,
                  "classPath": "%s",
                  "serverCount": %d
                }
                """.formatted(
                checkTibcoLib(),
                System.getProperty("java.class.path").replace("\\", "\\\\"),
                tibcoEmsQueueService.getAllServers().size()
        );
    }

    @GetMapping("/api/test-connection")
    @ResponseBody
    public String testConnection() {
        StringBuilder result = new StringBuilder();
        var servers = tibcoEmsQueueService.getAllServers();
        result.append("Testing ").append(servers.size()).append(" servers:\n\n");
        
        for (var server : servers) {
            result.append(server.getName()).append(" (").append(server.getHost())
                  .append(":").append(server.getPort()).append(")\n");
            var queues = tibcoEmsQueueService.getAllQueuesForServer(server.getName());
            result.append("  Queues found: ").append(queues.size()).append("\n");
            if (!queues.isEmpty()) {
                result.append("  Top 3:\n");
                queues.stream().limit(3).forEach(q -> 
                    result.append("    - ").append(q.getQueueName())
                          .append(": ").append(q.getMessageCount()).append(" msgs\n")
                );
            }
            result.append("\n");
        }
        
        return result.toString();
    }

    private boolean checkTibcoLib() {
        try {
            Class.forName("com.tibco.tibjms.admin.TibjmsAdmin");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @GetMapping("/api/queues/{serverName}")
    @ResponseBody
    public List<QueueInfo> getQueuesForServer(@PathVariable String serverName) {
        return tibcoEmsQueueService.getAllQueuesForServer(serverName);
    }

    @GetMapping("/api/configured-servers")
    @ResponseBody
    public List<Map<String, String>> getConfiguredServers() {
        Map<String, String> serverStatusMap = tibcoEmsQueueService.getServerStatus();
        return tibcoEmsQueueService.getAllServers()
                .stream()
                .map(server -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("name", server.getName());
                    map.put("status", serverStatusMap.getOrDefault(server.getName(), "UNKNOWN"));
                    return map;
                })
                .toList();
    }
}
