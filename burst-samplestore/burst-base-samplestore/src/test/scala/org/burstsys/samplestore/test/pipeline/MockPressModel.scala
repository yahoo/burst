/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.test.pipeline

object MockPressModel extends

  RootStructure(
    f0 = "rootytooty", f1 = 1234567890, f2 = 2222,

    f3 = SecondLevelStructure(
      f0 = 221, f1 = 222, f2 = 223.0,

      f3 = Array(

        ThirdLevelStructure(
          f0 = 71, f1 = 72,
          f2 = Map("-1" -> "0"),
          f3 = Array(0.1, 0.2)
        ),

        ThirdLevelStructure(
          f0 = 73, f1 = 74,
          f2 = Map("1" -> "2"),
          f3 = Array(0.3, 0.4)
        )

      ),

      f4 = ThirdLevelStructure(
        f0 = 75, f1 = 76,
        f2 = Map("3" -> "4", "5" -> "6"),
        f3 = Array(0.5, 0.6)
      )

    ),

    f4 = Array(

      SecondLevelStructure(
        f0 = 331, f1 = 332, f2 = 333.0,

        f3 = Array(

          ThirdLevelStructure(
            f0 = 31, f1 = 32,
            f2 = Map("7" -> "8", "9" -> "10", "11" -> "12"),
            f3 = Array(0.7, 0.8, 0.9)
          ),

          ThirdLevelStructure(
            f0 = 41, f1 = 42,
            f2 = Map("13" -> "14", "15" -> "16", "17" -> "18", "19" -> "20"),
            f3 = Array(1.0, 1.1, 1.2, 1.3)
          ),

          ThirdLevelStructure(
            f0 = 51, f1 = 52,
            f2 = Map("21" -> "22", "23" -> "24", "25" -> "26", "27" -> "28", "29" -> "30"),
            f3 = Array(1.4, 1.5, 1.6, 1.7, 1.8)
          )

        ),

        f4 = ThirdLevelStructure(
          f0 = 61, f1 = 62,
          f2 = Map("31" -> "32", "33" -> "34", "35" -> "36", "37" -> "38", "39" -> "40", "41" -> "42"),
          f3 = Array(1.9, 2.0, 2.1, 2.2, 2.3, 2.4)
        )

      )

    ),

    added = AddedStructure(
      f0 = "added",
      f1 = 666.666,
      f2 = Array("Hello", "Goodbye"),
      f3 = true,
      f4 = false
    ),

    application = ApplicationStructure(
      firstUse = UseStructure("firstUse"), // Unique "tags" to distinguish nodes
      mostUse = UseStructure("mostUse"),
      lastUse = UseStructure("lastUse")
    )

  )
