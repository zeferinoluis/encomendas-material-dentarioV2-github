package com.example.data

import java.util.Locale

data class CategoryInfo(
    val name: String,
    val emoji: String,
    val description: String,
    val colorHex: Long = 0xFF2563EB
)

data class CategoryStats(
    val categoryName: String,
    val categoryInfo: CategoryInfo,
    val totalProducts: Int,
    val selectedProductsCount: Int,
    val totalQuantity: Int
)

object CategoryUtils {

    val CATEGORIES = listOf(
        CategoryInfo(
            name = "Agulhas & Anestesia",
            emoji = "💉",
            description = "Agulhas, anestésicos, seringas de aspiração e carules",
            colorHex = 0xFF0284C7 // Sky Blue
        ),
        CategoryInfo(
            name = "Desinfecção & Esterilização",
            emoji = "🧼",
            description = "Desinfectantes de superfícies, aspiração, rolos e testes",
            colorHex = 0xFF0D9488 // Teal
        ),
        CategoryInfo(
            name = "Dentística & Restauração",
            emoji = "🦷",
            description = "Compósitos, adesivos, matrizes, tiras de lixa e gravadores",
            colorHex = 0xFF2563EB // Royal Blue
        ),
        CategoryInfo(
            name = "Cimentos & Adesivos",
            emoji = "🧴",
            description = "Cimentos de fixação, adesivos de garrafa e sistemas de cimentação",
            colorHex = 0xFFB45309 // Amber/Brown
        ),
        CategoryInfo(
            name = "Impressão & Prótese",
            emoji = "🧪",
            description = "Silicones, alginatos, gessos, ceras, moldeiras e material protético",
            colorHex = 0xFF7C3AED // Purple
        ),
        CategoryInfo(
            name = "Cunhas & Matrizes",
            emoji = "🔩",
            description = "Cunhas interdentárias, matrizes metálicas e sistemas de matriz seccional",
            colorHex = 0xFF6366F1 // Indigo
        ),
        CategoryInfo(
            name = "Rotativos & Brocas",
            emoji = "⚡",
            description = "Brocas de tungsténio, diamantes, turbinas e sprays",
            colorHex = 0xFFD97706 // Amber/Orange
        ),
        CategoryInfo(
            name = "Polimento & Acabamento",
            emoji = "💎",
            description = "Discos, escovas, pastas e sistemas de polimento e acabamento",
            colorHex = 0xFFCA8A04 // Gold
        ),
        CategoryInfo(
            name = "Endodontia & Cirurgia",
            emoji = "🩸",
            description = "Limas ProTaper, suturas de seda/silkam, alavancas e bisturis",
            colorHex = 0xFFDC2626 // Red
        ),
        CategoryInfo(
            name = "Implantologia",
            emoji = "🦴",
            description = "Implantes, pilares e material cirúrgico de implantologia",
            colorHex = 0xFF78716C // Stone
        ),
        CategoryInfo(
            name = "Postes & Espigões",
            emoji = "📌",
            description = "Postes de fibra, espigões e sistemas de reconstrução radicular",
            colorHex = 0xFFA21CAF // Fuchsia
        ),
        CategoryInfo(
            name = "Descartáveis & Proteção",
            emoji = "🛡️",
            description = "Babetes, copos, luvas, aspiradores, algodão e campos",
            colorHex = 0xFF059669 // Emerald Green
        ),
        CategoryInfo(
            name = "Higiene & Branqueamento",
            emoji = "✨",
            description = "Pasta profilaxia, escovas nylon, branqueamento e kits de prevenção",
            colorHex = 0xFFDB2777 // Pink
        ),
        CategoryInfo(
            name = "Instrumental Clínico",
            emoji = "🛠️",
            description = "Instrumental de consulta, cirurgia e manutenção de equipamento",
            colorHex = 0xFF334155 // Dark Slate
        ),
        CategoryInfo(
            name = "Radiologia",
            emoji = "🩻",
            description = "Protetores, sensores e material de radiografia",
            colorHex = 0xFF0891B2 // Cyan
        ),
        CategoryInfo(
            name = "Ortodontia",
            emoji = "😬",
            description = "Placas, ligaduras, arcos e material ortodôntico",
            colorHex = 0xFFF97316 // Orange
        ),
        CategoryInfo(
            name = "Equipamento Clínico",
            emoji = "🔌",
            description = "Turbinas, contra-ângulos e outros equipamentos de maior valor",
            colorHex = 0xFF1E293B // Slate 800
        ),
        CategoryInfo(
            name = "Outros Artigos",
            emoji = "📦",
            description = "Diversos materiais e acessórios médico-dentários",
            colorHex = 0xFF475569 // Slate
        )
    )

    private val categoryMap = CATEGORIES.associateBy { it.name }

    fun getCategoryInfo(categoryName: String): CategoryInfo {
        return categoryMap[categoryName] ?: CategoryInfo(
            name = categoryName,
            emoji = "🏷️",
            description = "Artigos da categoria $categoryName",
            colorHex = 0xFF475569
        )
    }

    /**
     * Determines the clean Portuguese category for a product based on explicit field or text matching.
     */
    fun getNormalizedCategory(product: Product): String {
        if (product.category.isNotBlank()) {
            val mapped = mapRawCategory(product.category)
            if (mapped != "Outros Artigos") return mapped
        }

        val text = "${product.description} ${product.company}".lowercase(Locale.ROOT)

        return when {
            text.containsAny("agulha", "anestes", "articaina", "lidocaina", "xilonibsa", "topigel", "normonjet", "seringa", "infiltra", "troncular") -> "Agulhas & Anestesia"
            text.containsAny("desinf", "esterili", "cavicide", "puli-jet", "helix", "sterigut", "toalhete", "autoclave", "detergente", "limpeza") -> "Desinfecção & Esterilização"
            text.containsAny("cimento", "cimentaç", "cement") -> "Cimentos & Adesivos"
            text.containsAny("composito", "adesivo", "etch", "bulk fill", "lixa", "polim", "maxcem", "restaur", "resina", "cera modelar", "match shade") -> "Dentística & Restauração"
            text.containsAny("cunha", "matriz") -> "Cunhas & Matrizes"
            text.containsAny("silicone", "alginato", "hydrogum", "occlufast", "putty", "impress", "molde", "gesso", "caixa aparelho", "protese", "prótese") -> "Impressão & Prótese"
            text.containsAny("broca", "diamante", "diam komet", "turbina", "nsk", "pana plus", "rotativo", "contra-angulo", "peca de mao", "spray lubrif") -> "Rotativos & Brocas"
            text.containsAny("polidor", "polimento", "acabamento", "escova polim") -> "Polimento & Acabamento"
            text.containsAny("protaper", "lima", "sutura", "silkam", "seda", "alavanca", "bisturi", "cirurg", "endodon", "gutapercha", "gelo") -> "Endodontia & Cirurgia"
            text.containsAny("implante", "pilar cirurgico", "implantologia") -> "Implantologia"
            text.containsAny("poste de fibra", "espigao", "espigão", "poste dentin") -> "Postes & Espigões"
            text.containsAny("babete", "copo", "luva", "aspirador", "algodao", "protector rvg", "mascara", "campo", "dodeira", "dedeira", "descart") -> "Descartáveis & Proteção"
            text.containsAny("pola night", "bleaching", "profilax", "escova nylon", "whitesmile", "branquea", "higiene") -> "Higiene & Branqueamento"
            text.containsAny("instrumental", "pinca", "espatula", "porta") -> "Instrumental Clínico"
            text.containsAny("radiograf", "rvg", "sensor rx", "colimador") -> "Radiologia"
            text.containsAny("ortodont", "bracket", "arco ortho", "ligadura", "placa clear") -> "Ortodontia"
            text.containsAny("equipamento", "compressor", "autoclave classe") -> "Equipamento Clínico"
            else -> "Outros Artigos"
        }
    }

    /**
     * Maps a raw category coming either from the Montellano catalogue (Spanish, catalog.csv)
     * or from the clinic's own purchase-history base (Portuguese, Base_Precos_Material_Dentario.xlsx)
     * to one of the app's [CATEGORIES]. Covers all 18 raw Montellano categories and all 14 raw
     * clinic categories 1:1, so only genuinely miscellaneous items ("VARIOS" / "Diversos") fall
     * back to "Outros Artigos".
     *
     * Matching is done on an accent-stripped, lower-cased copy of the raw text so that Spanish
     * ("RESTAURACIÓN") and Portuguese ("Restauração") spellings of the same word both match the
     * same accent-free keyword ("restaura").
     */
    private fun mapRawCategory(raw: String): String {
        val lower = stripDiacritics(raw.trim().lowercase(Locale.ROOT))
        return when {
            // Agulhas & Anestesia
            lower.containsAny("agujas y anestesias", "agujas", "anestes", "agulha") -> "Agulhas & Anestesia"
            // Desinfecção & Esterilização
            lower.containsAny("desinfec", "esterili") -> "Desinfecção & Esterilização"
            // Cimentos & Adesivos
            lower.containsAny("cementos", "cimento") -> "Cimentos & Adesivos"
            // Instrumental Clínico (checked before "Endodontia & Cirurgia" — the clinic's own
            // category "Instrumental Clínico e Cirurgia" contains the word "cirurgia" too)
            lower.contains("instrumental") -> "Instrumental Clínico"
            // Dentística & Restauração
            lower.containsAny("restaura", "compuesto", "adhesivo", "composito", "dentisteria") -> "Dentística & Restauração"
            // Cunhas & Matrizes
            lower.containsAny("cunas y matrices", "cunhas e matrizes", "matrices", "matrizes") -> "Cunhas & Matrizes"
            // Impressão & Prótese
            lower.containsAny("impres", "silicon", "alginat", "protesis", "protese", "gesso", "laboratorio") -> "Impressão & Prótese"
            // Rotativos & Brocas
            lower.containsAny("fresas", "rotatorio", "broca", "turbin", "diamantadas") -> "Rotativos & Brocas"
            // Polimento & Acabamento
            lower.containsAny("pulidores", "polimento", "acabamento") -> "Polimento & Acabamento"
            // Endodontia & Cirurgia
            lower.containsAny("endodoncia", "endodontia", "biomateriales y suturas", "cirugia", "cirurgia", "sutura", "lima") -> "Endodontia & Cirurgia"
            // Implantologia
            lower.containsAny("implantes", "implantologia") -> "Implantologia"
            // Postes & Espigões
            lower.containsAny("postes", "espigoes") -> "Postes & Espigões"
            // Descartáveis & Proteção
            lower.containsAny("desechables", "descartaveis", "proteccion", "protecao individual", "descart") -> "Descartáveis & Proteção"
            // Higiene & Branqueamento
            lower.containsAny("prevencion y profilaxis", "prevencao e profilaxia", "profilaxia", "blanqueamiento", "estetica e branqueamento", "higiene") -> "Higiene & Branqueamento"
            // Radiologia
            lower.containsAny("radiografia", "radiologia") -> "Radiologia"
            // Ortodontia
            lower.containsAny("ortodoncia", "ortodontia") -> "Ortodontia"
            // Equipamento Clínico
            lower.containsAny("equipamento clinico", "equipo") -> "Equipamento Clínico"
            // Explicit catch-alls
            lower.containsAny("varios", "diversos") -> "Outros Artigos"
            else -> "Outros Artigos"
        }
    }

    /**
     * Removes accents/diacritics (á, ã, ç, ñ, ó, …) so category text coming from Spanish or
     * Portuguese sources can be matched with a single set of plain keywords.
     */
    private fun stripDiacritics(input: String): String {
        val normalized = java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD)
        return normalized.replace(Regex("\\p{Mn}+"), "")
    }

    private fun String.containsAny(vararg keywords: String): Boolean {
        return keywords.any { this.contains(it) }
    }
}