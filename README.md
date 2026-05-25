# Transfer HTTP

O **Transfer HTTP** é um aplicativo Android que transforma seu dispositivo em um servidor de arquivos local. Ele permite que você gerencie, baixe e envie arquivos do seu computador (ou outros dispositivos na mesma rede) diretamente para o seu celular através de uma interface web intuitiva.

## Tecnologias Utilizadas

- **Linguagem:** [Kotlin](https://kotlinlang.org/)
- **UI:** [Jetpack Compose](https://developer.android.com/jetpack/compose) (Interface moderna e declarativa)
- **Servidor Web:** [Ktor](https://ktor.io/) (Engine CIO para alta performance)
- **Injeção de Dependência:** [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
- **Arquitetura:** Clean Architecture (Separação clara entre Data, Domain e UI)
- **Serviços:** Foreground Service (Para manter o servidor ativo mesmo com o app em segundo plano)
- **Serialização:** Kotlinx Serialization

## Funcionalidades

- **Servidor HTTP Local:** Inicie um servidor robusto diretamente do seu smartphone.
- **Interface Web Completa:** Gerencie seus arquivos através de qualquer navegador.
- **Navegação de Diretórios:** Explore as pastas do seu dispositivo remotamente.
- **Download e Upload:** Transfira arquivos entre dispositivos de forma rápida e sem cabos.
- **Gestão de Pastas:** Crie novos diretórios diretamente pela interface web.
- **Notificação de Status:** Acompanhe se o servidor está ativo e qual o IP de acesso através da barra de notificações.

## Como Executar o Projeto

1. Clone este repositório:
   ```bash
   git clone https://github.com/seu-usuario/TransferHTTP.git
   ```
2. Abra o projeto no **Android Studio**.
3. Sincronize o Gradle e instale as dependências.
4. Execute o app em um dispositivo físico ou emulador (conectado na mesma rede Wi-Fi).
5. Clique no botão de "Iniciar Servidor".
6. No seu navegador, digite o endereço IP exibido no app (ex: `http://192.168.1.5:8080`).

## Estrutura do Projeto

O projeto segue os princípios da **Clean Architecture**:

- `data/`: Implementações de repositórios, configuração do servidor Ktor e lógica de armazenamento.
- `domain/`: Modelos de dados e interfaces de repositórios (regras de negócio).
- `di/`: Módulos de injeção de dependência com Hilt.
- `service/`: `ServerForegroundService` para garantir a persistência do servidor.
- `ui/`: Componentes Compose, ViewModels e estados da interface do usuário.

---