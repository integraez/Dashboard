package com.integrationhub.dashboard;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {

    @GetMapping("/")
    @ResponseBody
    public String home() {
        return """
                <!doctype html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8" />
                    <meta name="viewport" content="width=device-width, initial-scale=1"/>
                    <title>Integration Hub Dashboard</title>
                    <style>
                        :root {
                            --bg: #0b1020;
                            --panel: rgba(255,255,255,0.06);
                            --text: rgba(255,255,255,0.90);
                            --muted: rgba(255,255,255,0.65);
                            --border: rgba(255,255,255,0.12);
                            --accent: #7c5cff;
                            --accent2: #23c483;
                            --shadow: 0 10px 30px rgba(0,0,0,0.35);
                        }
                        
                        * { box-sizing: border-box; }
                        
                        body {
                            margin: 0;
                            font-family: ui-sans-serif, system-ui, -apple-system, Segoe UI, Roboto, Helvetica, Arial;
                            color: var(--text);
                            background: radial-gradient(1200px 900px at 20% 10%, rgba(124,92,255,0.35), transparent 60%),
                                        radial-gradient(900px 700px at 90% 30%, rgba(35,196,131,0.25), transparent 55%),
                                        linear-gradient(180deg, #070b18, #0b1020 60%, #070b18);
                            min-height: 100vh;
                            display: flex;
                            justify-content: center;
                            align-items: center;
                            padding: 20px;
                        }
                        
                        .container {
                            background: var(--panel);
                            backdrop-filter: blur(10px);
                            border: 1px solid var(--border);
                            padding: 50px 40px;
                            border-radius: 20px;
                            box-shadow: var(--shadow);
                            text-align: center;
                            max-width: 600px;
                            width: 100%;
                        }
                        
                        .badge {
                            display: inline-block;
                            font-size: 13px;
                            color: rgba(255,255,255,0.85);
                            background: linear-gradient(90deg, rgba(124,92,255,0.9), rgba(35,196,131,0.75));
                            padding: 8px 16px;
                            border-radius: 999px;
                            box-shadow: 0 10px 24px rgba(0,0,0,0.25);
                            margin-bottom: 25px;
                            font-weight: 600;
                            letter-spacing: 0.3px;
                        }
                        
                        h1 {
                            color: var(--text);
                            margin-bottom: 15px;
                            font-size: 32px;
                            font-weight: 800;
                            letter-spacing: 0.3px;
                        }
                        
                        p {
                            color: var(--muted);
                            line-height: 1.6;
                            margin-bottom: 15px;
                            font-size: 15px;
                        }
                        
                        .status {
                            background: rgba(35,196,131,0.15);
                            color: rgba(35,196,131,1);
                            border: 1px solid rgba(35,196,131,0.35);
                            padding: 12px 24px;
                            border-radius: 12px;
                            display: inline-block;
                            margin-top: 25px;
                            font-size: 14px;
                            font-weight: 600;
                        }
                        
                        .btn {
                            background: linear-gradient(135deg, rgba(124,92,255,0.9), rgba(35,196,131,0.75));
                            color: white;
                            padding: 16px 32px;
                            border: none;
                            border-radius: 12px;
                            font-size: 15px;
                            font-weight: 600;
                            cursor: pointer;
                            text-decoration: none;
                            display: inline-block;
                            margin-top: 35px;
                            transition: all 0.3s ease;
                            box-shadow: 0 10px 25px rgba(124,92,255,0.3);
                            letter-spacing: 0.3px;
                        }
                        
                        .btn:hover {
                            transform: translateY(-2px);
                            box-shadow: 0 15px 35px rgba(124,92,255,0.4);
                        }
                        
                        .icon {
                            font-size: 48px;
                            margin-bottom: 20px;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="badge">Integration Dashboard</div>
                        <div class="icon">🚀</div>
                        <h1>Integration Hub Dashboard</h1>
                        <p>Welcome to your Integration Management Dashboard</p>
                        <p>The application is successfully running!</p>
                        <a href="/dashboard" class="btn">📊 View EMS Queue Dashboard</a></br>
                        <div class="status">✓ System Online</div>
                    </div>
                </body>
                </html>
                """;
    }
}
