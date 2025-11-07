package entity

/**
 * The ScoringCard class represents the cards that rule what scoring is used.
 *
 * @param [isRuleA] If true = rule A is used.
 * @param [animal] The animal the scoring rule is used for.
 */

class ScoringCard (
    var isRuleA: Boolean = false,
    val animal: Animal
)