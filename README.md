# Catálogo & Encomendas Dentárias — Montellano & Assisdent (Android)

Aplicação Android nativa desenvolvida em **Kotlin** e **Jetpack Compose** para consulta de catálogo e elaboração de encomendas de material médico-dentário da Clínica de Aver-o-Mar / Meadela.

## Funcionalidades Principais

- 🏥 **Catálogo Completo (69 Produtos)**: Todos os 69 produtos reais das faturas Montellano (31) e Assisdent (38).
- 🔍 **Pesquisa e Filtros em Tempo Real**: Pesquisa instantânea por código, descrição, fornecedor ou categoria, com filtros rápidos por "Todos", "Montellano", "Assisdent" e "Selecionados".
- 📦 **Controlo de Quantidades e Subtotais**: Controlos intuitivos (+ / - e caixa de seleção) com cálculo em tempo real do subtotal estimado (s/ IVA).
- 💾 **Persistência Local (Room Database)**: A sua encomenda e seleção de produtos ficam automaticamente guardados na base de dados SQLite local.
- 📜 **Histórico de Encomendas Guardadas**: Possibilidade de guardar encomendas anteriores com notas/observações personalizadas para consulta, cópia ou eliminação no futuro.
- 📤 **Exportação e Partilha**: Exportação direta do resumo da encomenda para texto formatado, cópia para a área de transferência ou partilha para WhatsApp / Email.

## Arquitetura Técnica

- **Linguagem**: Kotlin 100%
- **Interface**: Jetpack Compose com Material Design 3
- **Base de Dados**: Room Database com Kotlin Symbol Processing (KSP)
- **Gerenciamento de Estado**: Clean Architecture + MVVM (`ViewModel`, `StateFlow`, `collectAsStateWithLifecycle`)
