package com.example.data

import com.example.model.PointTier
import com.example.model.TriviaCategory
import com.example.model.TriviaQuestion

object TriviaQuestionBank {
    val questions: List<TriviaQuestion> = listOf(
        // ==================== GEOGRAPHY ====================
        // 10 pts
        TriviaQuestion(
            id = "geo_10_1",
            category = TriviaCategory.GEOGRAPHY,
            tier = PointTier.TIER_10,
            prompt = "Which is the largest ocean on Earth by surface area?",
            options = listOf("Atlantic Ocean", "Indian Ocean", "Pacific Ocean", "Arctic Ocean"),
            correctIndex = 2,
            explanation = "The Pacific Ocean covers more than 30% of Earth's surface."
        ),
        TriviaQuestion(
            id = "geo_10_2",
            category = TriviaCategory.GEOGRAPHY,
            tier = PointTier.TIER_10,
            prompt = "Cairo is the capital city of which country?",
            options = listOf("Morocco", "Egypt", "Jordan", "Tunisia"),
            correctIndex = 1,
            explanation = "Cairo sits near the Nile Delta and is the capital of Egypt."
        ),
        TriviaQuestion(
            id = "geo_10_3",
            category = TriviaCategory.GEOGRAPHY,
            tier = PointTier.TIER_10,
            prompt = "On which continent is the Sahara Desert located?",
            options = listOf("Asia", "Australia", "South America", "Africa"),
            correctIndex = 3,
            explanation = "The Sahara spans much of North Africa."
        ),
        // 30 pts
        TriviaQuestion(
            id = "geo_30_1",
            category = TriviaCategory.GEOGRAPHY,
            tier = PointTier.TIER_30,
            prompt = "Which river flows directly through the city of Budapest?",
            options = listOf("Rhine", "Danube", "Elbe", "Vistula"),
            correctIndex = 1,
            explanation = "The Danube separates Buda and Pest in Hungary's capital."
        ),
        TriviaQuestion(
            id = "geo_30_2",
            category = TriviaCategory.GEOGRAPHY,
            tier = PointTier.TIER_30,
            prompt = "Which country has the longest coastline in the world?",
            options = listOf("Russia", "Indonesia", "Canada", "Australia"),
            correctIndex = 2,
            explanation = "Canada's coastline stretches over 202,000 kilometers."
        ),
        TriviaQuestion(
            id = "geo_30_3",
            category = TriviaCategory.GEOGRAPHY,
            tier = PointTier.TIER_30,
            prompt = "Mount Kilimanjaro is located in which African nation?",
            options = listOf("Kenya", "Tanzania", "Ethiopia", "Uganda"),
            correctIndex = 1,
            explanation = "Kilimanjaro rises in northeastern Tanzania near the Kenyan border."
        ),
        // 50 pts
        TriviaQuestion(
            id = "geo_50_1",
            category = TriviaCategory.GEOGRAPHY,
            tier = PointTier.TIER_50,
            prompt = "Which South American country is completely landlocked alongside Paraguay?",
            options = listOf("Uruguay", "Ecuador", "Bolivia", "Suriname"),
            correctIndex = 2,
            explanation = "Bolivia and Paraguay are South America's only two landlocked countries."
        ),
        TriviaQuestion(
            id = "geo_50_2",
            category = TriviaCategory.GEOGRAPHY,
            tier = PointTier.TIER_50,
            prompt = "What strait separates Spain from Morocco?",
            options = listOf("Strait of Hormuz", "Strait of Gibraltar", "Bering Strait", "Strait of Malacca"),
            correctIndex = 1,
            explanation = "The Strait of Gibraltar connects the Atlantic Ocean to the Mediterranean Sea."
        ),
        TriviaQuestion(
            id = "geo_50_3",
            category = TriviaCategory.GEOGRAPHY,
            tier = PointTier.TIER_50,
            prompt = "Reykjavik is the world's northernmost capital of a sovereign state. Which island nation is it in?",
            options = listOf("Greenland", "Iceland", "Norway", "Faroe Islands"),
            correctIndex = 1,
            explanation = "Reykjavik is the capital of Iceland at roughly 64°N latitude."
        ),
        // 100 pts
        TriviaQuestion(
            id = "geo_100_1",
            category = TriviaCategory.GEOGRAPHY,
            tier = PointTier.TIER_100,
            prompt = "Which sovereign nation is completely enclaved within the borders of South Africa?",
            options = listOf("Eswatini", "Lesotho", "Botswana", "Namibia"),
            correctIndex = 1,
            explanation = "Lesotho is entirely surrounded by South Africa, whereas Eswatini borders Mozambique as well."
        ),
        TriviaQuestion(
            id = "geo_100_2",
            category = TriviaCategory.GEOGRAPHY,
            tier = PointTier.TIER_100,
            prompt = "Lake Baikal, the world's deepest freshwater lake, lies in which region?",
            options = listOf("Siberia, Russia", "Northern Kazakhstan", "Western Mongolia", "Manchuria, China"),
            correctIndex = 0,
            explanation = "Lake Baikal in southern Siberia reaches a maximum depth of 1,642 meters."
        ),
        TriviaQuestion(
            id = "geo_100_3",
            category = TriviaCategory.GEOGRAPHY,
            tier = PointTier.TIER_100,
            prompt = "What is the official capital city of Kazakhstan since its 2022 name restoration?",
            options = listOf("Almaty", "Astana", "Bishkek", "Tashkent"),
            correctIndex = 1,
            explanation = "Kazakhstan restored its capital's name from Nur-Sultan back to Astana in 2022."
        ),

        // ==================== MATHS ====================
        // 10 pts
        TriviaQuestion(
            id = "math_10_1",
            category = TriviaCategory.MATHS,
            tier = PointTier.TIER_10,
            prompt = "What is the value of 15 × 6?",
            options = listOf("75", "80", "90", "95"),
            correctIndex = 2,
            explanation = "15 × 6 = 90."
        ),
        TriviaQuestion(
            id = "math_10_2",
            category = TriviaCategory.MATHS,
            tier = PointTier.TIER_10,
            prompt = "What is the sum of the interior angles of any triangle?",
            options = listOf("90°", "180°", "270°", "360°"),
            correctIndex = 1,
            explanation = "Euclidean triangles always have interior angles summing to 180°."
        ),
        TriviaQuestion(
            id = "math_10_3",
            category = TriviaCategory.MATHS,
            tier = PointTier.TIER_10,
            prompt = "Which of these numbers is a prime number?",
            options = listOf("21", "27", "29", "33"),
            correctIndex = 2,
            explanation = "29 has only two positive divisors: 1 and 29."
        ),
        // 30 pts
        TriviaQuestion(
            id = "math_30_1",
            category = TriviaCategory.MATHS,
            tier = PointTier.TIER_30,
            prompt = "If 3x + 7 = 28, what is the value of x?",
            options = listOf("6", "7", "8", "9"),
            correctIndex = 1,
            explanation = "3x = 21, so x = 7."
        ),
        TriviaQuestion(
            id = "math_30_2",
            category = TriviaCategory.MATHS,
            tier = PointTier.TIER_30,
            prompt = "What is 25% of 240?",
            options = listOf("48", "50", "60", "80"),
            correctIndex = 2,
            explanation = "25% is one quarter; 240 ÷ 4 = 60."
        ),
        TriviaQuestion(
            id = "math_30_3",
            category = TriviaCategory.MATHS,
            tier = PointTier.TIER_30,
            prompt = "A right triangle has legs of length 6 and 8. What is the length of its hypotenuse?",
            options = listOf("9", "10", "12", "14"),
            correctIndex = 1,
            explanation = "By the Pythagorean theorem, √(6² + 8²) = √(36 + 64) = √100 = 10."
        ),
        // 50 pts
        TriviaQuestion(
            id = "math_50_1",
            category = TriviaCategory.MATHS,
            tier = PointTier.TIER_50,
            prompt = "What is the value of 2⁵ + 3³?",
            options = listOf("49", "55", "59", "61"),
            correctIndex = 2,
            explanation = "2⁵ = 32 and 3³ = 27, so 32 + 27 = 59."
        ),
        TriviaQuestion(
            id = "math_50_2",
            category = TriviaCategory.MATHS,
            tier = PointTier.TIER_50,
            prompt = "What is the next number in the Fibonacci sequence: 1, 1, 2, 3, 5, 8, 13, 21, ...?",
            options = listOf("29", "32", "34", "35"),
            correctIndex = 2,
            explanation = "Each term is the sum of the two preceding terms: 13 + 21 = 34."
        ),
        TriviaQuestion(
            id = "math_50_3",
            category = TriviaCategory.MATHS,
            tier = PointTier.TIER_50,
            prompt = "If log₁₀(x) = 3, what is x?",
            options = listOf("30", "100", "300", "1000"),
            correctIndex = 3,
            explanation = "log₁₀(x) = 3 means 10³ = x = 1000."
        ),
        // 100 pts
        TriviaQuestion(
            id = "math_100_1",
            category = TriviaCategory.MATHS,
            tier = PointTier.TIER_100,
            prompt = "How many ways can 5 distinct books be arranged on a shelf (5!)?",
            options = listOf("60", "100", "120", "150"),
            correctIndex = 2,
            explanation = "5! = 5 × 4 × 3 × 2 × 1 = 120."
        ),
        TriviaQuestion(
            id = "math_100_2",
            category = TriviaCategory.MATHS,
            tier = PointTier.TIER_100,
            prompt = "What is the sum of the first 10 positive integers (1 + 2 + ... + 10)?",
            options = listOf("45", "50", "55", "60"),
            correctIndex = 2,
            explanation = "Using n(n + 1)/2 = 10 × 11 / 2 = 55."
        ),
        TriviaQuestion(
            id = "math_100_3",
            category = TriviaCategory.MATHS,
            tier = PointTier.TIER_100,
            prompt = "What is the derivative of f(x) = x³ − 4x at x = 2?",
            options = listOf("4", "8", "10", "12"),
            correctIndex = 1,
            explanation = "f'(x) = 3x² − 4; at x = 2, f'(2) = 3(4) − 4 = 8."
        ),

        // ==================== SCIENCE ====================
        // 10 pts
        TriviaQuestion(
            id = "sci_10_1",
            category = TriviaCategory.SCIENCE,
            tier = PointTier.TIER_10,
            prompt = "What is the chemical symbol for water?",
            options = listOf("HO₂", "H₂O", "O₂H", "H₂O₂"),
            correctIndex = 1,
            explanation = "Each water molecule contains two hydrogen atoms and one oxygen atom."
        ),
        TriviaQuestion(
            id = "sci_10_2",
            category = TriviaCategory.SCIENCE,
            tier = PointTier.TIER_10,
            prompt = "Which planet in our solar system is known as the Red Planet?",
            options = listOf("Venus", "Jupiter", "Mars", "Saturn"),
            correctIndex = 2,
            explanation = "Iron oxide dust on Mars's surface gives it a reddish hue."
        ),
        TriviaQuestion(
            id = "sci_10_3",
            category = TriviaCategory.SCIENCE,
            tier = PointTier.TIER_10,
            prompt = "Which gas makes up roughly 78% of Earth's atmosphere?",
            options = listOf("Oxygen", "Carbon Dioxide", "Argon", "Nitrogen"),
            correctIndex = 3,
            explanation = "Dry air is about 78% nitrogen and 21% oxygen."
        ),
        // 30 pts
        TriviaQuestion(
            id = "sci_30_1",
            category = TriviaCategory.SCIENCE,
            tier = PointTier.TIER_30,
            prompt = "Which organelle is known as the powerhouse of a eukaryotic cell because it generates ATP?",
            options = listOf("Ribosome", "Golgi apparatus", "Mitochondrion", "Lysosome"),
            correctIndex = 2,
            explanation = "Mitochondria produce most of a cell's supply of adenosine triphosphate (ATP)."
        ),
        TriviaQuestion(
            id = "sci_30_2",
            category = TriviaCategory.SCIENCE,
            tier = PointTier.TIER_30,
            prompt = "What is the SI unit of electrical resistance?",
            options = listOf("Volt", "Ampere", "Watt", "Ohm"),
            correctIndex = 3,
            explanation = "Resistance is measured in ohms (Ω), named after Georg Simon Ohm."
        ),
        TriviaQuestion(
            id = "sci_30_3",
            category = TriviaCategory.SCIENCE,
            tier = PointTier.TIER_30,
            prompt = "Which element has the atomic number 6 and forms the backbone of organic molecules?",
            options = listOf("Nitrogen", "Carbon", "Silicon", "Boron"),
            correctIndex = 1,
            explanation = "Carbon has 6 protons and forms four covalent bonds."
        ),
        // 50 pts
        TriviaQuestion(
            id = "sci_50_1",
            category = TriviaCategory.SCIENCE,
            tier = PointTier.TIER_50,
            prompt = "At standard atmospheric pressure, at what temperature are Celsius and Fahrenheit equal?",
            options = listOf("−32°", "−40°", "0°", "−273°"),
            correctIndex = 1,
            explanation = "−40°C equals −40°F on both temperature scales."
        ),
        TriviaQuestion(
            id = "sci_50_2",
            category = TriviaCategory.SCIENCE,
            tier = PointTier.TIER_50,
            prompt = "Which subatomic particle carries no electric charge?",
            options = listOf("Proton", "Positron", "Electron", "Neutron"),
            correctIndex = 3,
            explanation = "Neutrons reside in the atomic nucleus with zero net electric charge."
        ),
        TriviaQuestion(
            id = "sci_50_3",
            category = TriviaCategory.SCIENCE,
            tier = PointTier.TIER_50,
            prompt = "Approximately how long does light from the Sun take to reach Earth?",
            options = listOf("8 seconds", "8 minutes", "8 hours", "8 days"),
            correctIndex = 1,
            explanation = "Sunlight travels ~150 million km in roughly 8 minutes and 20 seconds."
        ),
        // 100 pts
        TriviaQuestion(
            id = "sci_100_1",
            category = TriviaCategory.SCIENCE,
            tier = PointTier.TIER_100,
            prompt = "Which law states that the entropy of an isolated system never decreases over time?",
            options = listOf(
                "Zeroth Law of Thermodynamics",
                "First Law of Thermodynamics",
                "Second Law of Thermodynamics",
                "Third Law of Thermodynamics"
            ),
            correctIndex = 2,
            explanation = "The Second Law describes the irreversibility of natural processes via increasing entropy."
        ),
        TriviaQuestion(
            id = "sci_100_2",
            category = TriviaCategory.SCIENCE,
            tier = PointTier.TIER_100,
            prompt = "What enzyme unwinds the DNA double helix ahead of replication?",
            options = listOf("Ligase", "Helicase", "Polymerase", "Primase"),
            correctIndex = 1,
            explanation = "DNA helicase separates the two complementary strands by breaking hydrogen bonds."
        ),
        TriviaQuestion(
            id = "sci_100_3",
            category = TriviaCategory.SCIENCE,
            tier = PointTier.TIER_100,
            prompt = "What is the escape velocity from Earth's surface (rounded to the nearest km/s)?",
            options = listOf("7.9 km/s", "11.2 km/s", "16.7 km/s", "24.5 km/s"),
            correctIndex = 1,
            explanation = "An unpropelled object needs ~11.2 km/s to escape Earth's gravitational pull."
        ),

        // ==================== LITERATURE ====================
        // 10 pts
        TriviaQuestion(
            id = "lit_10_1",
            category = TriviaCategory.LITERATURE,
            tier = PointTier.TIER_10,
            prompt = "Who wrote the tragedy 'Romeo and Juliet'?",
            options = listOf("Charles Dickens", "William Shakespeare", "Christopher Marlowe", "Jane Austen"),
            correctIndex = 1,
            explanation = "William Shakespeare wrote Romeo and Juliet early in his career around 1595."
        ),
        TriviaQuestion(
            id = "lit_10_2",
            category = TriviaCategory.LITERATURE,
            tier = PointTier.TIER_10,
            prompt = "In George Orwell's '1984', what is the name of the totalitarian surveillance figure?",
            options = listOf("Big Brother", "The Captain", "Mr. Jones", "O'Brien"),
            correctIndex = 0,
            explanation = "'Big Brother is watching you' is the famous slogan of Oceania's regime."
        ),
        TriviaQuestion(
            id = "lit_10_3",
            category = TriviaCategory.LITERATURE,
            tier = PointTier.TIER_10,
            prompt = "Who is the detective residing at 221B Baker Street?",
            options = listOf("Hercule Poirot", "Philip Marlowe", "Sherlock Holmes", "Dupin"),
            correctIndex = 2,
            explanation = "Arthur Conan Doyle placed Sherlock Holmes at 221B Baker Street in London."
        ),
        // 30 pts
        TriviaQuestion(
            id = "lit_30_1",
            category = TriviaCategory.LITERATURE,
            tier = PointTier.TIER_30,
            prompt = "Which novel opens with the line, 'Call me Ishmael'?",
            options = listOf("Treasure Island", "Moby-Dick", "The Old Man and the Sea", "Robinson Crusoe"),
            correctIndex = 1,
            explanation = "Herman Melville's 1851 epic Moby-Dick opens with narrator Ishmael."
        ),
        TriviaQuestion(
            id = "lit_30_2",
            category = TriviaCategory.LITERATURE,
            tier = PointTier.TIER_30,
            prompt = "Who authored the classic 1960 novel 'To Kill a Mockingbird'?",
            options = listOf("Harper Lee", "Toni Morrison", "Flannery O'Connor", "Carson McCullers"),
            correctIndex = 0,
            explanation = "Harper Lee won the Pulitzer Prize for To Kill a Mockingbird."
        ),
        TriviaQuestion(
            id = "lit_30_3",
            category = TriviaCategory.LITERATURE,
            tier = PointTier.TIER_30,
            prompt = "In Homer's 'The Odyssey', what is the name of Odysseus's loyal wife?",
            options = listOf("Helen", "Clytemnestra", "Penelope", "Andromache"),
            correctIndex = 2,
            explanation = "Penelope awaits Odysseus's return to Ithaca for twenty years."
        ),
        // 50 pts
        TriviaQuestion(
            id = "lit_50_1",
            category = TriviaCategory.LITERATURE,
            tier = PointTier.TIER_50,
            prompt = "Which Russian author wrote 'Crime and Punishment'?",
            options = listOf("Leo Tolstoy", "Anton Chekhov", "Fyodor Dostoevsky", "Ivan Turgenev"),
            correctIndex = 2,
            explanation = "Fyodor Dostoevsky published Crime and Punishment in 1866."
        ),
        TriviaQuestion(
            id = "lit_50_2",
            category = TriviaCategory.LITERATURE,
            tier = PointTier.TIER_50,
            prompt = "In 'The Great Gatsby', what color is the light at the end of Daisy Buchanan's dock?",
            options = listOf("Amber", "Blue", "White", "Green"),
            correctIndex = 3,
            explanation = "The green light symbolizes Gatsby's unreachable hopes and dreams."
        ),
        TriviaQuestion(
            id = "lit_50_3",
            category = TriviaCategory.LITERATURE,
            tier = PointTier.TIER_50,
            prompt = "Which 19th-century novel has the subtitle 'The Modern Prometheus'?",
            options = listOf("Dracula", "Frankenstein", "Jane Eyre", "Wuthering Heights"),
            correctIndex = 1,
            explanation = "Mary Shelley subtitled Frankenstein 'or, The Modern Prometheus' in 1818."
        ),
        // 100 pts
        TriviaQuestion(
            id = "lit_100_1",
            category = TriviaCategory.LITERATURE,
            tier = PointTier.TIER_100,
            prompt = "Who wrote the magical realist masterpiece 'One Hundred Years of Solitude'?",
            options = listOf(
                "Jorge Luis Borges",
                "Isabel Allende",
                "Gabriel García Márquez",
                "Mario Vargas Llosa"
            ),
            correctIndex = 2,
            explanation = "Nobel laureate Gabriel García Márquez chronicled the Buendía family of Macondo."
        ),
        TriviaQuestion(
            id = "lit_100_2",
            category = TriviaCategory.LITERATURE,
            tier = PointTier.TIER_100,
            prompt = "In Dante's 'Inferno', which ancient Roman poet guides Dante through Hell?",
            options = listOf("Ovid", "Virgil", "Horace", "Cicero"),
            correctIndex = 1,
            explanation = "Virgil serves as Dante's guide through the Inferno and Purgatorio."
        ),
        TriviaQuestion(
            id = "lit_100_3",
            category = TriviaCategory.LITERATURE,
            tier = PointTier.TIER_100,
            prompt = "Which modernist novel by James Joyce follows Leopold Bloom through Dublin on June 16, 1904?",
            options = listOf("Dubliners", "Ulysses", "Finnegans Wake", "A Portrait of the Artist"),
            correctIndex = 1,
            explanation = "Ulysses unfolds over a single day—now celebrated as Bloomsday."
        )
    )

    fun pickQuestion(
        category: TriviaCategory,
        tier: PointTier,
        answeredIds: List<String>
    ): TriviaQuestion {
        val pool = questions.filter { it.category == category && it.tier == tier }
        val unplayed = pool.filterNot { it.id in answeredIds }
        return if (unplayed.isNotEmpty()) unplayed.first() else pool.random()
    }
}
