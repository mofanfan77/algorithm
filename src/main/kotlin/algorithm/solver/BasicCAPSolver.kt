package solver

import catalog.extensions.ConstraintType
import catalog.extensions.ObjectiveType
import catalog.extensions.VariableType
import core.engine.DefaultEngine
import core.entity.Coordinate
import core.entity.NodeEntity
import data.dataobject.CAPResultData
import data.dataobject.CAPTraceData
import utility.Algorithms

open class BasicCAPSolver : CAPSolver() {
    private lateinit var engine: DefaultEngine

    init {
        createEngine()
        createProblem()
    }

    final override fun createEngine() {
        this.engine = Algorithms.createDefaultSolver("CraneAssignment")
    }

    /**
     * 必须调用清理方法
     */
    override fun dispose() {
        this.problem.dispose()
        this.solution.dispose()
        this.engine.dispose()
    }

    override fun export(path: String) {
        this.logout(engine)
    }

    // *********************************** 二阶段模型
    /**
     * 生成变量
     */
    private fun addBasicVariablesStep2(model: DefaultEngine) {
        val cars = problem.getResource()
        val tasks = problem.getTask()
        // start and end time
        for (t in 1..timeHorizon) {
            for (i in 1..tasks.size) {
                for (j in 1..cars.size) {
                    model.addNumVars(0.0, 1.0, 0.0, VariableType.Integer, "O_${i}_${j}_${t}")
                    model.addNumVars(0.0, 1.0, 0.0, VariableType.Float, "F_${i}_${j}_${t}")
                }
            }
            for (j in 1..cars.size) {
                // 使用车的移动范围
                val cr = cars[j - 1]
                model.addNumVars(cr.lowBound, minOf(maxLength, cr.upBound), 0.0, VariableType.Float, "S_${j}_${t}")
                model.addNumVars(0.0, maxLength, 0.0, VariableType.Float, "Displace_${j}_${t}")
                model.addNumVars(0.0, 1.0, 0.0, VariableType.Float, "L_${j}_${t}")
            }
        }
        // dest of task i
        for (i in 1..problem.getTask().size) {
            model.addNumVars(0.0, maxLength, 0.0, VariableType.Float, "Origin_${i}")
            model.addNumVars(0.0, maxLength, 0.0, VariableType.Float, "Destin_${i}")
            model.addNumVars(0.0, timeHorizon.toDouble(), 0.0, VariableType.Float, "Lower_finishTime_${i}")
            model.addNumVars(0.0, timeHorizon.toDouble(), 0.0, VariableType.Float, "Upper_finishTime_${i}")
            for (k in 1..problem.getTask()[i - 1].destCandidates.size) {
                model.addNumVars(0.0, 1.0, 0.0, VariableType.Integer, "K_${i}_${k}")
            }
        }
        model.addNumVars(0.0, timeHorizon.toDouble(), 0.0, VariableType.Float, "last_finish_task")
        solverLog.info("total generate variables ${model.getNVars()}")
    }

    private fun addFinishTimeConstraints(model: DefaultEngine) {
        for (i in 1..problem.getTask().size) {
            var task = problem.getTask()[i - 1]
            var constr = model.linearNumExpr()
            var constr2 = model.linearNumExpr()
            for (j in 1..problem.getResource().size) {
                for (t in 1..timeHorizon) {
                    constr.addTerm(t.toDouble(), model.getVarByName("F_${i}_${j}_${t}"))
                    constr2.addTerm(t.toDouble(), model.getVarByName("F_${i}_${j}_${t}"))
                }
            }
            constr.addTerm(-1.0, model.getVarByName("Lower_finishTime_${i}"))
            constr2.addTerm(1.0, model.getVarByName("Upper_finishTime_${i}"))
            model.addConstr(constr, ConstraintType.le, task.dueTime, "task_finish_time1_${i}")
            model.addConstr(constr2, ConstraintType.ge, task.earlyTime, "task_finish_time2_${i}")
        }
    }

    private fun addDisplacementConstrains(model: DefaultEngine) {
        for (j in 1..problem.getResource().size) {
            for (t in 2..timeHorizon) {
                var varName = "Displace_${j}_${t}"
                var constr = model.linearNumExpr()
                var constr2 = model.linearNumExpr()
                constr.addTerm(1.0, model.getVarByName("S_${j}_${t}"))
                constr.addTerm(-1.0, model.getVarByName("S_${j}_${t - 1}"))
                constr.addTerm(-1.0, model.getVarByName(varName))
                constr2.addTerm(1.0, model.getVarByName("S_${j}_${t - 1}"))
                constr2.addTerm(-1.0, model.getVarByName("S_${j}_${t}"))
                constr2.addTerm(-1.0, model.getVarByName(varName))
                model.addConstr(constr, ConstraintType.le, 0.0, varName + "constr1")
                model.addConstr(constr2, ConstraintType.le, 0.0, varName + "constr2")
            }
        }
    }

    private fun addUniqueConstraints(model: DefaultEngine) {
        // 一个任务分配给唯一(车辆,时间点)
        // 一个(车辆,时间点)只有一个任务
        var cars = problem.getResource()
        var tasks = problem.getTask()
        for (i in 1..tasks.size) {
            var taskUnique = model.linearNumExpr()
            for (j in 1..cars.size) {
                for (t in 1..timeHorizon) {
                    taskUnique.addTerm(1.0, model.getVarByName("O_${i}_${j}_${t}"))
                }
            }
            model.addConstr(taskUnique, ConstraintType.le, 1.0, "constr_crane_task_unique_${i}")
        }
        for (j in 1..cars.size) {
            for (t in 1..timeHorizon) {
                var taskUnique = model.linearNumExpr()
                for (i in 1..tasks.size) {
                    taskUnique.addTerm(1.0, model.getVarByName("O_${i}_${j}_${t}"))
                }
                model.addConstr(taskUnique, ConstraintType.le, 1.0, "constr_crane_task_unique2_${j}_${t}")
            }
        }
    }

    private fun addOriginConstraints(model: DefaultEngine) {
        for (i in 1..problem.getTask().size) {
            var task = problem.getTask()[i - 1]
            var constr = model.linearNumExpr()
            constr.addTerm(1.0, model.getVarByName("Origin_${i}"))
            if (task.dependentTask == null) {//没有起点依赖
                model.addConstr(constr, ConstraintType.eq, task.origin.x, "origin_constr_${i}")
            } else {
                var dependentNr = task.dependentTask!!.algoNr
                constr.addTerm(-1.0, model.getVarByName("Destin_${dependentNr}"))
                model.addConstr(constr, ConstraintType.eq, 0.0, "origin_constr_${i}")
            }
        }
    }

    private fun addDestinationSelectionConstraints(model: DefaultEngine) {
        for (i in 1..problem.getTask().size) {
            var task = problem.getTask()[i - 1]
            var constr = model.linearNumExpr()
            constr.addTerm(1.0, model.getVarByName("Destin_${i}"))
            if (task.destCandidates.isEmpty() || !task.destSelectionMode) {
                model.addConstr(constr, ConstraintType.eq, task.dest.x, "destionation_constr_${i}")
            } else {
                var constr2 = model.linearNumExpr()
                for (k in 1..task.destCandidates.size) {
                    constr.addTerm(-task.destCandidates[k - 1].x, model.getVarByName("K_${i}_${k}"))
                    constr2.addTerm(1.0, model.getVarByName("K_${i}_${k}"))
                }
                model.addConstr(constr, ConstraintType.eq, 0.0, "destionation_constr_${i}")
                model.addConstr(constr2, ConstraintType.eq, 1.0, "destionation_unique_constr_${i}")
            }
        }
    }

    private fun addInitialLocationConstraints(model: DefaultEngine) {
        for (j in 1..problem.getResource().size) {
            var constr = model.linearNumExpr()
            constr.addTerm(1.0, model.getVarByName("S_${j}_1"))
            model.addConstr(constr, ConstraintType.eq, problem.getResource()[j - 1].x, "initial_S_${j}")
        }
    }

    private fun addDistanceLimitConstraints(model: DefaultEngine) {
        for (t in 1..timeHorizon) {
            for (j in 2..problem.getResource().size) {
                var distanceConstr = model.linearNumExpr()
                distanceConstr.addTerm(1.0, model.getVarByName("S_${j}_${t}"))
                distanceConstr.addTerm(-1.0, model.getVarByName("S_${j - 1}_${t}"))
                model.addConstr(distanceConstr, ConstraintType.ge, safetyInterval, "safeDistance_${j}_${t}")
            }
        }
    }

    private fun addSpeedLimitConstraints(model: DefaultEngine) {
        var cars = problem.getResource()
        for (j in 1..cars.size) {
            for (t in 1 until timeHorizon) {
                var speedConstr = model.linearNumExpr()
                speedConstr.addTerm(1.0, model.getVarByName("S_${j}_${t}"))
                speedConstr.addTerm(-1.0, model.getVarByName("S_${j}_${t + 1}"))
                model.addConstr(speedConstr, ConstraintType.le, cars[j - 1].speed, "left_speed_limit_${j}_${t}")
                model.addConstr(speedConstr, ConstraintType.ge, -cars[j - 1].speed, "right_speed_limit_${j}_${t}")
            }
        }
    }

    private fun addLocationConstraints(model: DefaultEngine) {
        var cars = problem.getResource()
        var tasks = problem.getTask()
        for (t in 1..timeHorizon) {
            for (j in 1..cars.size) {
                for (i in 1..tasks.size) {
                    var origDelay = tasks[i - 1].originDelay
                    var destDelay = tasks[i - 1].destDelay
                    var skipDest = false
                    var skipOrig = false
                    if (t - destDelay <= 0) { // 该车不能对这个任务进行作业
                        model.getVarByName("F_${i}_${j}_${t}")!!.getXCore()!!.ub = 0.0
                        skipDest = true
                    }
                    if (t + origDelay > timeHorizon) {
                        model.getVarByName("O_${i}_${j}_${t}")!!.getXCore()!!.ub = 0.0
                        skipOrig = true
                    }
                    // startTime
                    if (!skipOrig) {
                        var leftConstr = model.linearNumExpr()
                        leftConstr.addTerm(1.0, model.getVarByName("S_${j}_${t + origDelay}"))
                        leftConstr.addTerm(-maxLength, model.getVarByName("O_${i}_${j}_${t}"))
                        leftConstr.addTerm(-1.0, model.getVarByName("Origin_${i}"))
                        model.addConstr(leftConstr, ConstraintType.ge, -maxLength, "left_constr_location_${i}_${j}_${t}")
                        var rightConstr = model.linearNumExpr()
                        rightConstr.addTerm(1.0, model.getVarByName("S_${j}_${t + origDelay}"))
                        rightConstr.addTerm(maxLength, model.getVarByName("O_${i}_${j}_${t}"))
                        rightConstr.addTerm(-1.0, model.getVarByName("Origin_${i}"))
                        model.addConstr(rightConstr, ConstraintType.le, maxLength, "right_constr_location_${i}_${j}_${t}")
                        // consider orig delay
                        var origDelayConstr = model.linearNumExpr()
                        origDelayConstr.addTerm(1.0, model.getVarByName("S_${j}_${t + origDelay}"))
                        origDelayConstr.addTerm(-1.0, model.getVarByName("S_${j}_${t}"))
                        origDelayConstr.addTerm(maxLength, model.getVarByName("O_${i}_${j}_${t}"))
                        model.addConstr(origDelayConstr, ConstraintType.le, maxLength, "origdelay_left_${i}_${j}_${t}")
                        var origDelayConstr2 = model.linearNumExpr()
                        origDelayConstr2.addTerm(1.0, model.getVarByName("S_${j}_${t + origDelay}"))
                        origDelayConstr2.addTerm(-1.0, model.getVarByName("S_${j}_${t}"))
                        origDelayConstr2.addTerm(-maxLength, model.getVarByName("O_${i}_${j}_${t}"))
                        model.addConstr(origDelayConstr2, ConstraintType.ge, -maxLength, "origdelay_right_${i}_${j}_${t}")
                    }
                    // consider dest delay
                    if (!skipDest) {
                        var destDelayConstr = model.linearNumExpr()
                        destDelayConstr.addTerm(1.0, model.getVarByName("S_${j}_${t - destDelay}"))
                        destDelayConstr.addTerm(-1.0, model.getVarByName("S_${j}_${t}"))
                        destDelayConstr.addTerm(maxLength, model.getVarByName("F_${i}_${j}_${t}"))
                        model.addConstr(destDelayConstr, ConstraintType.le, maxLength, "destdelay_left_${i}_${j}_${t}")
                        var destDelayConstr2 = model.linearNumExpr()
                        destDelayConstr2.addTerm(1.0, model.getVarByName("S_${j}_${t - destDelay}"))
                        destDelayConstr2.addTerm(-1.0, model.getVarByName("S_${j}_${t}"))
                        destDelayConstr2.addTerm(-maxLength, model.getVarByName("F_${i}_${j}_${t}"))
                        model.addConstr(destDelayConstr2, ConstraintType.ge, -maxLength, "destdelay_right_${i}_${j}_${t}")
                        // endTime
                        var leftConstr2 = model.linearNumExpr()
                        leftConstr2.addTerm(1.0, model.getVarByName("S_${j}_${t - destDelay}"))
                        leftConstr2.addTerm(-maxLength, model.getVarByName("F_${i}_${j}_${t}"))
                        leftConstr2.addTerm(-1.0, model.getVarByName("Destin_${i}"))
                        model.addConstr(leftConstr2, ConstraintType.ge, -maxLength, "left2_constr_location_${i}_${j}_${t}")
                        var rightConstr2 = model.linearNumExpr()
                        rightConstr2.addTerm(1.0, model.getVarByName("S_${j}_${t - destDelay}"))
                        rightConstr2.addTerm(-1.0, model.getVarByName("Destin_${i}"))
                        rightConstr2.addTerm(maxLength, model.getVarByName("F_${i}_${j}_${t}"))
                        model.addConstr(rightConstr2, ConstraintType.le, maxLength, "right2_constr_location_${i}_${j}_${t}")
                    }
                }
            }
        }
    }

    private fun addWorkFlagConstraints(model: DefaultEngine) {
        for (j in 1..problem.getResource().size) {
            for (t in 1..timeHorizon) {
                var constr1 = model.linearNumExpr()
                constr1.addTerm(1.0, model.getVarByName("L_${j}_${t}"))
                if (t > 1) {
                    constr1.addTerm(-1.0, model.getVarByName("L_${j}_${t - 1}"))
                }
                for (i in 1..problem.getTask().size) {
                    constr1.addTerm(-1.0, model.getVarByName("O_${i}_${j}_${t}"))
                    constr1.addTerm(1.0, model.getVarByName("F_${i}_${j}_${t}"))
                }
                model.addConstr(constr1, ConstraintType.eq, 0.0, "workFlag_${j}_${t}")
            }
        }
    }

    private fun addTimeBasicConstraints(model: DefaultEngine) {
        for (i in 1..problem.getTask().size) {
            for (j in 1..problem.getResource().size) {
                var car = problem.getResource()[j - 1]
                var task = problem.getTask()[i - 1]
                var timeUnit = ((task.getDistance() / car.speed) + 1).toInt() + task.destDelay + task.originDelay
                for (t in 1..timeHorizon) {
                    if (t + timeUnit <= timeHorizon) {
                        var constr1 = model.linearNumExpr()
                        constr1.addTerm(1.0, model.getVarByName("O_${i}_${j}_${t}"))
                        constr1.addTerm(-1.0, model.getVarByName("F_${i}_${j}_${t + timeUnit}"))
                        model.addConstr(constr1, ConstraintType.eq, 0.0, "time_balance_${i}_${j}_${t}")
                    } else {
                        var modify = model.getVarByName("O_${i}_${j}_${t}")
                        modify!!.getXCore()!!.ub = 0.0
                    }
                    if (t <= timeUnit) {
                        var modify = model.getVarByName("F_${i}_${j}_${t}")
                        modify!!.getXCore()!!.ub = 0.0
                    }
                }
            }
        }
    }

    private fun addLatestFinishTaskConstraint(model: DefaultEngine) {
        for (i in 1..problem.getTask().size) {
            for (t in 1..timeHorizon) {
                for (j in 1..problem.getResource().size) {
                    var constr = model.linearNumExpr()
                    constr.addTerm(1.0, model.getVarByName("last_finish_task"))
                    constr.addTerm(-t.toDouble(), model.getVarByName("F_${i}_${j}_${t}"))
                    model.addConstr(constr, ConstraintType.ge, 0.0, "last_finish_task_${i}_${j}_${t}")
                }
            }
        }
    }

    /**
     * 考虑不同行车状态的约束
     */
    private fun addConstraintsForCranes(model: DefaultEngine) {
        for (j in 1..problem.getResource().size) {
            var constr = model.linearNumExpr()
            var car = problem.getResource()[j - 1]
            if (car.status != 0) { // 检修
                for (t in 1..timeHorizon) {
                    for (i in 1..problem.getTask().size) {
                        constr.addTerm(1.0, model.getVarByName("O_${i}_${j}_${t}"))
                    }
                }
            }
            if (car.status == 2) { // 死车不能动
                for (t in 2..timeHorizon) {
                    var vars = model.getVarByName("Displace_${j}_${t}")
                    vars!!.getXCore()!!.ub = 0.0
                }
                solverLog.info("存在死车检修${car.resourceId}")
            }
            model.addConstr(constr, ConstraintType.le, 0.0, "constr_status_car${j}")
        }
    }

    open fun addSpecialConstraints(model: DefaultEngine) {

    }

    /**
     * 依赖任务 相关约束
     * 1. 行车不变约束
     * 2. 开始时间
     */
    private fun addDependentTaskConstraints(model: DefaultEngine) {
        for (i in 1..problem.getTask().size) {
            var task = problem.getTask()[i - 1]
            // tx - ty >= m(2-x-y)
            // ty - tx <= m(2-x-y)
            var sameCraneConstr = model.linearNumExpr()
            var sameCraneConstr2 = model.linearNumExpr()
            // 时间前后约束
            // fix 不用equal 存在依赖任务无法分配的情况 t*O + maxT (1 - O) >= RHS
            var timeDependentConstr = model.linearNumExpr()
            // O <= F_依赖
            var timeDependentConstr2 = model.linearNumExpr()
            for (j in 1..problem.getResource().size) {
                for (t in 1..timeHorizon) {
                    if (task.dependentTask != null) {
                        var i2 = task.dependentTask!!.algoNr
                        if (!task.canChangeCraneFlag) { // 有依赖且不能换
                            sameCraneConstr.addTerm(j.toDouble() + timeHorizon.toDouble(), model.getVarByName("O_${i}_${j}_${t}"))
                            sameCraneConstr.addTerm(-j.toDouble() + timeHorizon.toDouble(), model.getVarByName("O_${i2}_${j}_${t}"))
                            sameCraneConstr2.addTerm(j.toDouble() - timeHorizon.toDouble(), model.getVarByName("O_${i}_${j}_${t}"))
                            sameCraneConstr2.addTerm(-j.toDouble() - timeHorizon.toDouble(), model.getVarByName("O_${i2}_${j}_${t}"))
                        }
                        // 时间1
                        timeDependentConstr.addTerm(t.toDouble(), model.getVarByName("O_${i}_${j}_${t}"))
                        timeDependentConstr.addTerm(-timeHorizon.toDouble(), model.getVarByName("O_${i}_${j}_${t}"))
                        timeDependentConstr.addTerm(-t.toDouble(), model.getVarByName("F_${i2}_${j}_${t}"))
                        // 时间2
                        timeDependentConstr2.addTerm(1.0, model.getVarByName("O_${i}_${j}_${t}"))
                        timeDependentConstr2.addTerm(-1.0, model.getVarByName("F_${i2}_${j}_${t}"))
                    }
                }
            }
            model.addConstr(sameCraneConstr, ConstraintType.le, 2 * timeHorizon.toDouble(), "ub_dependent_task_${i}")
            model.addConstr(sameCraneConstr2, ConstraintType.ge, -2 * timeHorizon.toDouble(), "lb_dependent_task_${i}")
            model.addConstr(timeDependentConstr, ConstraintType.ge, -timeHorizon.toDouble(), "constr1_time_depend_task_${i}")
            model.addConstr(timeDependentConstr2, ConstraintType.le, 0.0, "constr2_time_depend_task_${i}")
        }
    }

    private fun addConstraintsStep2(model: DefaultEngine) {
        addUniqueConstraints(model)
        addDependentTaskConstraints(model)
        addDisplacementConstrains(model)
        addFinishTimeConstraints(model)
        addDistanceLimitConstraints(model)
        addOriginConstraints(model)
        addDestinationSelectionConstraints(model)
        addInitialLocationConstraints(model)
        addLocationConstraints(model)
        addTimeBasicConstraints(model)
        addWorkFlagConstraints(model)
        addLatestFinishTaskConstraint(model)
        addSpeedLimitConstraints(model)
        addConstraintsForCranes(model)
        addSpecialConstraints(model)
        solverLog.info("total generate constraints ${model.getNCons()}")
    }

    private fun addObjectiveStep2(model: DefaultEngine) {
        var obj = model.linearNumExpr()
        obj.addTerm(5.0 / timeHorizon, model.getVarByName("last_finish_task"))
        for (i in 1..problem.getTask().size) {
            for (j in 1..problem.getResource().size) {
                for (t in 1..timeHorizon) {
                    obj.addTerm(-50.0, model.getVarByName("F_${i}_${j}_${t}"))
                }
            }
            obj.addTerm(1.0, model.getVarByName("Lower_finishTime_${i}"))
            obj.addTerm(1.0, model.getVarByName("Upper_finishTime_${i}"))
        }
        for (j in 1..problem.getResource().size) {
            for (t in 1..timeHorizon) {
                obj.addTerm(1.0 / (timeHorizon * maxLength), model.getVarByName("Displace_${j}_${t}"))
            }
        }
        model.setObjective(obj, ObjectiveType.Minimize)
    }

    open fun buildModel() {
        createModel(config.nodeSelection)
    }

    private fun createModel(pairingMode: Boolean = false) {
        if (pairingMode) {
            this.solveOriginDestinationPair()
        }
        this.buildCraneAssignmentProblem()
    }

    override fun solveModel() {
        // 默认参数
        engine.tuneParameter()
        // 求解时间
        engine.setTimeLimit(config.timeLimit)
        var solveFlag = engine.solve()
        solverLog.info("best objective is ${engine.getBestSolObj()}")
        if (solveFlag) {
            this.process(engine)
        }
        this.logout(engine)
        this.dispose()
    }

    /**
     * 尝试lp建模
     */
    open fun buildCraneAssignmentProblem() {
        this.addBasicVariablesStep2(engine)
        this.addConstraintsStep2(engine)
        this.addObjectiveStep2(engine)
    }

    /**
     * 打印日志文件
     */
    private fun logout(model: DefaultEngine) {
        var path = ".\\src\\test\\resources\\output\\result.lp"
        try {
            if (config.logFlag) {
                if (config.logPath != "") {
                    path = config.logPath
                }
                model.export(path)
            }
        } catch (e: Exception) {
            solverLog.warn("fail to print log to ${path} ___ 日志打印失败,")
        }
    }

    private fun process(model: DefaultEngine, flag: Int = 0) {
        var printFlag = true
        if (flag == 3) {
            for (i in 1..problem.getTask().size) {
                var value1 = model.getBestSolValFromName("Upper_finishTime_${i}")
                var value2 = model.getBestSolValFromName("Lower_finishTime_${i}")
                println("任务${i}, upper = ${value1}, lower =${value2}")
            }
        } else if (flag == 2) {
            for (j in 1..problem.getResource().size) {
                for (t in 1..timeHorizon) {
                    for (i in 1..problem.getTask().size) {
                        var value = model.getBestSolValFromName("O_${i}_${j}_${t}")
                        if (value > 0.1) {
                            println("resource $j start task $i at time $t, located at ${problem.getTask()[i - 1].origin.x}")
                        }
                        var value2 = model.getBestSolValFromName("F_${i}_${j}_${t}")
                        if (value2 > 0.1) {
                            println("resource $j finish task $i at time $t, located at ${problem.getTask()[i - 1].dest.x}")
                        }
                    }
                }
            }
        } else if (flag == 1) {
            for (j in 1..problem.getResource().size) {
                for (t in 1..timeHorizon) {
                    for (i in 1..problem.getTask().size) {
                        var car = problem.getResource()[j - 1]
                        var task = problem.getTask()[i - 1]
                        var value = model.getBestSolValFromName("O_${i}_${j}_${t}")
                        if (value > 0.1) {
                            println("resource ${car.resourceId} work with task ${task.taskId}")
                        }
                    }
                }
            }
        } else { // 不打印
            printFlag = false
            var tempResult = mutableMapOf<String, CAPResultData>()
            for (j in 1..problem.getResource().size) {
                for (t in 1..timeHorizon) {
                    // trace
                    var value = model.getBestSolValFromName("S_${j}_${t}")
                    var trace = CAPTraceData()
                    var car = problem.getResource()[j - 1]
                    trace.craneId = car.resourceId
                    trace.timeUnit = t
                    trace.x = value
                    solution.addTrace(trace)
                    for (i in 1..problem.getTask().size) {
                        var task = problem.getTask()[i - 1].taskId
                        tempResult.putIfAbsent(task, CAPResultData())
                        var value = model.getBestSolValFromName("O_${i}_${j}_${t}")
                        var value2 = model.getBestSolValFromName("F_${i}_${j}_${t}")
                        if (value > 0.1) {
                            tempResult[task]!!.startTime = t
                            tempResult[task]!!.craneId = car.resourceId
                        }
                        if (value2 > 0.1) {
                            tempResult[task]!!.endTime = t
                            tempResult[task]!!.taskId = task
                        }
                    }
                }
            }
            tempResult.values.forEach { solution.addAssignment(it) }
        }
    }

    // *********************************** 一阶段模型
    /**
     * 规划起点终点
     */
    private fun solveOriginDestinationPair() {
        var model = DefaultEngine("originDestinationPair")
        var tempResult = this.addBasicVariablesStep1(model)
        var tempSource = tempResult.first
        this.addConstraintsStep1(model, tempResult)
        this.addObjectiveStep1(model, tempSource)
        model.tuneParameter()
        model.solve()
        model.export(".\\src\\main\\resource\\output\\result_step1.lp")
        this.processStep1(model, tempSource)
        solverLog.info("best objective is ${model.getBestSolObj()}")
    }

    private fun addBasicVariablesStep1(model: DefaultEngine): Pair<MutableMap<NodeEntity, ArrayList<String>>, MutableMap<NodeEntity, ArrayList<String>>> {
        // 用于存放生成的节点列表 (Nodes, it sourceNodes (变量名))
        var sourceNodes = mutableMapOf<NodeEntity, ArrayList<String>>()
        var sinkNodes = mutableMapOf<NodeEntity, ArrayList<String>>()
        // 任务中包含的移动任务
        for (i in 1..problem.getTask().size) {
            var task = problem.getTask()[i - 1]
            if (task.dependentTask == null) { // 无前序
                for (dst in task.destCandidates) {
                    if (!dst.invalidFlag()) {
                        var varName = "node_from_${task.algoNr}_to_${dst.nodeName}"
                        var orgNode = task.origin
                        model.addNumVars(0.0, 1.0, 0.0, VariableType.Integer, varName)
                        sourceNodes.putIfAbsent(dst, arrayListOf())
                        sinkNodes.putIfAbsent(orgNode, arrayListOf())
                        sourceNodes[dst]!!.add(varName)
                        sinkNodes[orgNode]!!.add(varName)
                    }
                }
            } else { // 有前序
                for (org in task.dependentTask!!.destCandidates.filter { !it.invalidFlag() }) { // 过滤有效节点
                    for (dst in task.destCandidates.filter { !it.invalidFlag() }) {
                        var varName = "dependent_node_from_${task.algoNr}_${org.nodeName}_to_${dst.nodeName}"
                        model.addNumVars(0.0, 1.0, 0.0, VariableType.Integer, varName)
                        sourceNodes.putIfAbsent(dst, arrayListOf())
                        sinkNodes.putIfAbsent(org, arrayListOf())
                        sourceNodes[dst]!!.add(varName)
                        sinkNodes[org]!!.add(varName)
                    }
                }
            }
        }
        // 不包含的移动任务（RX)
        for (node in problem.getNode()) {
            if (node.isOccupied) {
                for (dst in problem.repairList) {
                    if (!dst.invalidFlag()) {
                        model.addNumVars(0.0, 1.0, 0.0, VariableType.Integer, "supple_task_${node.nodeName}_to_${dst.nodeName}")
                    }
                }
            }
        }
        return Pair(sourceNodes, sinkNodes)
    }

    private fun addConstraintsStep1(model: DefaultEngine, tempResult: Pair<MutableMap<NodeEntity, ArrayList<String>>, MutableMap<NodeEntity, ArrayList<String>>>) {
        var tempSource = tempResult.first
        var tempSink = tempResult.second
        this.addOneLocationConstraints(model)
        this.addMoveBlockConstraints(model, tempSource, tempSink)
        this.addConsecutiveConstraints(model, tempSource)
    }

    private fun addConsecutiveConstraints(model: DefaultEngine, tempSource: MutableMap<NodeEntity, ArrayList<String>>) {
        for (i in 1..problem.getTask().size) {
            var task = problem.getTask()[i - 1]
            if (task.dependentTask != null) {
                var preced = task.dependentTask!!
                if (preced.dependentTask == null) { // 头序任务
                    for (dst1 in preced.destCandidates.filter { !it.invalidFlag() }) {
                        var varName = "node_from_${preced.algoNr}_to_${dst1.nodeName}"
                        var constr = model.linearNumExpr()
                        constr.addTerm(1.0, model.getVarByName(varName))
                        for (dst2 in task.destCandidates.filter { !it.invalidFlag() }) {
                            var varName2 = "dependent_node_from_${task.algoNr}_${dst1.nodeName}_to_${dst2.nodeName}"
                            constr.addTerm(-1.0, model.getVarByName(varName2))
                        }
                        model.addConstr(constr, ConstraintType.eq, 0.0, "node_Consecutive_${task.algoNr}")
                    }
                } else { // 中间序任务
                    for (dst1 in preced.destCandidates.filter { !it.invalidFlag() }) {
                        var constr = model.linearNumExpr()
                        for (dst0 in preced.dependentTask!!.destCandidates) {
                            var varName0 = "dependent_node_from_${preced.algoNr}_${dst0.nodeName}_to_${dst1.nodeName}"
                            constr.addTerm(1.0, model.getVarByName(varName0))
                        }
                        for (dst2 in task.destCandidates.filter { !it.invalidFlag() }) {
                            var varName2 = "dependent_node_from_${task.algoNr}_${dst1.nodeName}_to_${dst2.nodeName}"
                            constr.addTerm(-1.0, model.getVarByName(varName2))
                        }
                        model.addConstr(constr, ConstraintType.eq, 0.0, "node_Consecutive_${task.algoNr}")
                    }
                }

            }
        }
    }

    private fun addOneLocationConstraints(model: DefaultEngine) {
        for (i in 1..problem.getTask().size) {
            var task = problem.getTask()[i - 1]
            var constr = model.linearNumExpr()
            if (task.dependentTask == null) {
                for (dst in task.destCandidates) {
                    if (!dst.invalidFlag()) {
                        constr.addTerm(1.0, model.getVarByName("node_from_${task.algoNr}_to_${dst.nodeName}"))
                    }
                }
            } else { // 有前序， 用前序的终点集合
                for (org in task.dependentTask!!.destCandidates.filter { !it.invalidFlag() }) { // 过滤有效节点
                    for (dst in task.destCandidates.filter { !it.invalidFlag() }) {
                        constr.addTerm(1.0, model.getVarByName("dependent_node_from_${task.algoNr}_${org.nodeName}_to_${dst.nodeName}"))
                    }
                }
            }
            model.addConstr(constr, ConstraintType.eq, 1.0, "dst_selection_${task.algoNr}")
        }
    }

    /**
     * 工位需求溢出时，需要生成新任务将现有任务移除
     */
    private fun addMoveBlockConstraints(model: DefaultEngine, tempSource: MutableMap<NodeEntity, ArrayList<String>>, tempSink: MutableMap<NodeEntity, ArrayList<String>>) {
        for (node in problem.getNode()) {
            if (node.isOccupied) {
                var constr = model.linearNumExpr()
                for (dst in problem.repairList) {
                    if (!dst.invalidFlag()) {
                        constr.addTerm(1.0, model.getVarByName("supple_task_${node.nodeName}_to_${dst.nodeName}"))
                    }
                }
                tempSource[node]?.forEach {
                    constr.addTerm(-1.0, model.getVarByName(it))
                }
                tempSink[node]?.forEach {
                    constr.addTerm(1.0, model.getVarByName(it))
                }
                model.addConstr(constr, ConstraintType.ge, 0.0, "supple_node_constr_${node.nodeName}")
            }
        }
    }

    private fun addObjectiveStep1(model: DefaultEngine, tempSource: MutableMap<NodeEntity, ArrayList<String>>) {
        // 时间成本 = 无依赖时间成本+ 有依赖时间成本 + 补充移动时间成本 + 节点重复时间成本
        var objective = model.linearNumExpr()
        for (task in problem.getTask()) {
            if (task.dependentTask == null) {
                for (dst in task.destCandidates.filter { !it.invalidFlag() }) {
                    var distance = Coordinate.getOneDimDistance(task.origin, dst)
                    objective.addTerm(distance, model.getVarByName("node_from_${task.algoNr}_to_${dst.nodeName}"))
                }
            } else { // 有前序依赖
                for (org in task.dependentTask!!.destCandidates.filter { !it.invalidFlag() }) { // 过滤有效节点
                    for (dst in task.destCandidates.filter { !it.invalidFlag() }) {
                        var varName = "dependent_node_from_${task.algoNr}_${org.nodeName}_to_${dst.nodeName}"
                        var distance = Coordinate.getOneDimDistance(org, dst)
                        objective.addTerm(distance, model.getVarByName(varName))
                    }
                }
            }
        }
        // 补充移动时间成本
        for (node in problem.getNode()) {
            if (node.isOccupied) {
                for (dst in problem.repairList) {
                    if (!dst.invalidFlag()) {
                        var varName = "supple_task_${node.nodeName}_to_${dst.nodeName}"
                        var distance = Coordinate.getOneDimDistance(node, dst)
                        objective.addTerm(distance, model.getVarByName(varName))
                    }
                }
            }
        }
        // 节点重复时间成本 （ 相对独立，遂写入objective模块... 后续可以拆分搬运回variable/constraint方法....）
        for ((node, varList) in tempSource) {
            var varName = "node_exceed_punish_${node.nodeName}"
            var constr = model.linearNumExpr()
            model.addNumVars(0.0, problem.getTask().size.toDouble(), 0.0, VariableType.Float, varName)
            constr.addTerm(1.0, model.getVarByName(varName))
            varList.forEach {
                constr.addTerm(-1.0, model.getVarByName(it))
            }
            model.addConstr(constr, ConstraintType.ge, -1.0, "constr_$varName")
            objective.addTerm(600.0, model.getVarByName(varName))
        }
        model.setObjective(objective)
    }

    private fun processStep1(model: DefaultEngine, tempSource: MutableMap<NodeEntity, ArrayList<String>>) {
        for ((node, varList) in tempSource) {
            var varName = "node_exceed_punish_${node.nodeName}"
            var value = model.getBestSolValFromName(varName)
            println("${varName}, value = ${value}")
        }
        for (node in problem.getNode()) {
            if (node.isOccupied) {
                for (dst in problem.repairList) {
                    if (!dst.invalidFlag()) {
                        var name = "supple_task_${node.nodeName}_to_${dst.nodeName}"
                        var value = model.getBestSolValFromName(name)
                        if (value > 0.1) {
                            println("新增任务 起始于 ${node.nodeName}目的地 ${dst.nodeName}")
                        }
                    }
                }
            }
        }
        for (i in 1..problem.getTask().size) {
            var task = problem.getTask()[i - 1]
            if (task.dependentTask == null) { // 无前序
                for (dst in task.destCandidates.filter { !it.invalidFlag() }) {
                    var varName = "node_from_${task.algoNr}_to_${dst.nodeName}"
                    var value = model.getBestSolValFromName(varName)
                    if (value > 0.1) {
                        task.dest = dst
                        println("任务${task.taskId} 起始于 ${task.origin.nodeName}目的地 ${dst.nodeName}")
                    }
                }
            } else { // 有前序
                for (org in task.dependentTask!!.destCandidates.filter { !it.invalidFlag() }) { // 过滤有效节点
                    for (dst in task.destCandidates.filter { !it.invalidFlag() }) {
                        var varName = "dependent_node_from_${task.algoNr}_${org.nodeName}_to_${dst.nodeName}"
                        var value = model.getBestSolValFromName(varName)
                        if (value > 0.1) {
                            task.origin = org
                            task.dest = dst
                            println("依赖任务${task.taskId},任务id ${task.algoNr}  起始于 ${org.nodeName} 目的地 ${dst.nodeName}, value = $value")
                        }
                    }
                }
            }
        }
    }
}