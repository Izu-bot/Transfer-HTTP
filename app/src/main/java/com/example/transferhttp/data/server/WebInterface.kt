package com.example.transferhttp.data.server

object WebInterface {
    val HTML_INTERFACE = $$"""
        <!DOCTYPE html>
        <html lang="pt">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Transfer HTTP - Gestor de Diretórios</title>
<style>
                * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; }
                body { background-color: #f4f6f9; color: #333; padding: 20px; display: flex; flex-direction: column; min-height: 100vh; }
                .container { max-width: 1000px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); flex: 1; width: 100%; }
                header { display: flex; justify-content: space-between; align-items: center; border-bottom: 2px solid #eee; padding-bottom: 15px; margin-bottom: 20px; }
                h1 { color: #2c3e50; font-size: 24px; }
                .path-bar { background: #eef2f5; padding: 10px; border-radius: 4px; font-family: monospace; font-size: 14px; margin-bottom: 20px; display: flex; align-items: center; }
                .actions { display: flex; gap: 10px; }
                button, .btn { background: #3498db; color: white; border: none; padding: 10px 15px; border-radius: 4px; cursor: pointer; font-weight: bold; font-size: 14px; display: inline-flex; align-items: center; text-decoration: none; }
                button:hover, .btn:hover { background: #2980b9; }
                .btn-danger { background: #e74c3c; }
                .btn-danger:hover { background: #c0392b; }
                .btn-success { background: #2ecc71; }
                .btn-success:hover { background: #27ae60; }
                table { width: 100%; border-collapse: collapse; margin-top: 10px; }
                th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
                th { background-color: #f8f9fa; color: #555; }
                tr:hover { background-color: #f1f4f6; }
                .icon { margin-right: 8px; font-size: 18px; }
                .folder { color: #f39c12; cursor: pointer; font-weight: bold; }
                .file { color: #2c3e50; }
                .empty-msg { text-align: center; color: #7f8c8d; padding: 20px; }
                #fileInput { display: none; }
                
                /* ESTILOS DO FOOTER */
                footer { text-align: center; padding: 20px 0; margin-top: 30px; color: #7f8c8d; font-size: 14px; border-top: 1px solid #e1e8ed; width: 100%; }
                footer a { color: #3498db; text-decoration: none; font-weight: bold; }
                footer a:hover { text-decoration: underline; }
                .heart { color: #e74c3c; display: inline-block; animation: heartbeat 1.5s infinite; }
                @keyframes heartbeat {
                    0% { transform: scale(1); }
                    20% { transform: scale(1.2); }
                    40% { transform: scale(1); }
                    60% { transform: scale(1.2); }
                    100% { transform: scale(1); }
                }
            </style>
        </head>
        <body>
            <div class="container">
                <header>
                    <h1>Transfer HTTP - Armazenamento</h1>
                    <div class="actions">
                        <button type="button" onclick="document.getElementById('fileInput').click()" class="btn-success">▲ Enviar Diretório</button>
                        <button type="button" onclick="createNewFolder()">+ Nova Pasta</button>
                        <button type="button" onclick="goBack()" class="btn-danger">◀ Voltar</button>
                        <input type="file" id="fileInput" onchange="uploadFile()" />
                    </div>
                </header>

                <div class="path-bar">
                    <span><strong>Caminho Atual:</strong> <span id="currentPathLabel">A carregar...</span></span>
                </div>

                <table>
                    <thead>
                        <tr>
                            <th>Nome</th>
                            <th>Tamanho</th>
                            <th>Ações</th>
                        </tr>
                    </thead>
                    <tbody id="fileListBody">
                    </tbody>
                </table>
                <div id="emptyMessage" class="empty-msg" style="display: none;">Esta pasta está vazia.</div>
            </div>

            <footer>
                <p>Desenvolvido com <span class="heart">❤</span> por <strong>Izu Bot</strong></p>
                <p style="margin-top: 5px; font-size: 12px;">&copy; 2026 Transfer HTTP - Mobile Server. Todos os direitos reservados.</p>
            </footer>

            <script>
                let currentPath = "";
                let pathHistory = [];

                async function loadFiles(path = "") {
                    console.log("Pedindo diretórios para rota", path);
                    try {
                        const url = path ? `/api/files?path=${encodeURIComponent(path)}` : '/api/files';
                        const response = await fetch(url);
                        
                        if (!response.ok) throw new Error("Falha na rede: " + response.status);
                        
                        const files = await response.json();
                        console.log("Diretórios recebidos:", files.length);
                        
                        if (!currentPath && files.length > 0) {
                            const samplePath = files[0].path;
                            currentPath = samplePath.substring(0, samplePath.lastIndexOf('/'));
                        } else if (path) {
                            currentPath = path;
                        }

                        document.getElementById('currentPathLabel').innerText = currentPath;
                        renderFiles(files);
                    } catch (error) {
                        console.error("Erro completo:", error);
                        alert("Erro ao carregar diretórios. Verifique a consola do navegador.");
                    }
                }

                function renderFiles(files) {
                    const tbody = document.getElementById('fileListBody');
                    const emptyMsg = document.getElementById('emptyMessage');
                    tbody.innerHTML = "";

                    if (files.length === 0) {
                        emptyMsg.style.display = "block";
                        return;
                    } else {
                        emptyMsg.style.display = "none";
                    }

                    files.forEach(file => {
                        const tr = document.createElement('tr');
                        
                        // Proteção contra caminhos com aspas simples (ex: "Pasta d'agua")
                        const safePath = file.path.replace(/'/g, "\\'");
                        
                        const tdName = document.createElement('td');
                        if (file.isDirectory) {
                            tdName.innerHTML = `<span class="icon">📁</span><span class="folder" onclick="navigateTo('${safePath}')">${file.name}</span>`;
                        } else {
                            tdName.innerHTML = `<span class="icon">📄</span><span class="file">${file.name}</span>`;
                        }
                        
                        const tdSize = document.createElement('td');
                        tdSize.innerText = file.isDirectory ? "-" : formatBytes(file.size);

                        const tdActions = document.createElement('td');
                        if (!file.isDirectory) {
                            tdActions.innerHTML = `<a href="/api/download?path=${encodeURIComponent(file.path)}" class="btn" style="padding: 5px 10px; font-size: 12px;">Download</a>`;
                        } else {
                            tdActions.innerText = "-";
                        }

                        tr.appendChild(tdName);
                        tr.appendChild(tdSize);
                        tr.appendChild(tdActions);
                        tbody.appendChild(tr);
                    });
                }

                function navigateTo(path) {
                    pathHistory.push(currentPath);
                    loadFiles(path);
                }

                function goBack() {
                    if (pathHistory.length > 0) {
                        const previousPath = pathHistory.pop();
                        loadFiles(previousPath);
                    } else {
                        const lastSlash = currentPath.lastIndexOf('/');
                        if (lastSlash > 2) { 
                            const parentPath = currentPath.substring(0, lastSlash);
                            loadFiles(parentPath);
                        } else {
                            alert("Já está na pasta raiz!");
                        }
                    }
                }

                async function createNewFolder() {
                    const folderName = prompt("Introduza o nome da nova pasta:");
                    if (!folderName) return;

                    try {
                        const response = await fetch(`/api/mkdir?parentPath=${encodeURIComponent(currentPath)}&name=${encodeURIComponent(folderName)}`, { method: 'POST' });
                        if (response.ok) {
                            loadFiles(currentPath);
                        } else {
                            alert("Erro ao criar pasta.");
                        }
                    } catch (error) {
                        alert("Erro na ligação: " + error);
                    }
                }

                async function uploadFile() {
                    const fileInput = document.getElementById('fileInput');
                    if (fileInput.files.length === 0) return;

                    const file = fileInput.files[0];
                    const formData = new FormData();
                    formData.append("file", file);

                    try {
                        document.getElementById('currentPathLabel').innerText = "A enviar diretório... Por favor, aguarde.";
                        const response = await fetch(`/api/upload?path=${encodeURIComponent(currentPath)}`, { method: 'POST', body: formData });

                        if (response.ok) {
                            alert("Diretório enviado com sucesso!");
                        } else {
                            alert("Falha no upload do diretório.");
                        }
                        loadFiles(currentPath);
                    } catch (error) {
                        alert("Erro no upload: " + error);
                        loadFiles(currentPath);
                    }
                    fileInput.value = "";
                }

                function formatBytes(bytes, decimals = 2) {
                    if (bytes === 0) return '0 Bytes';
                    const k = 1024;
                    const dm = decimals < 0 ? 0 : decimals;
                    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
                    const i = Math.floor(Math.log(bytes) / Math.log(k));
                    return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
                }

                window.onload = () => loadFiles();
            </script>
        </body>
        </html>
    """.trimIndent()
}