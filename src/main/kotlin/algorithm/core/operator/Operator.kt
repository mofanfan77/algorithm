package org.example.algorithm.core.operator

import kotlin.random.Random


abstract class Operator {
    val EPS = 0.0001
    open var name = ""
    var selectedTimes = 0
    var score = 0.0

    var probability = 0.0
    var weight = 1.0
    var lastSelected = false

    lateinit var random : Random

    constructor()
    constructor(rd: Random){
        this.random = rd
    }

    private fun selected() {
        selectedTimes += 1
        lastSelected = true
    }

    private fun updateScore(bonus: Double) {
        if (lastSelected) {
            score += bonus
        }
        lastSelected = false
    }

    private fun updateWeight(alpha: Double) {
        weight = alpha * weight + (1 - alpha) * (score / (EPS + selectedTimes)) // 避免除0
    }

    fun updateProbability(total: Double) {
        probability = weight / total
    }

    fun bonusAction(score: Double, alpha: Double) {
        this.selected()
        this.updateScore(score)
        this.updateWeight(alpha)
    }

}