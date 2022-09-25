package com.example.poketouch.emulator

import WasmBoy
import android.app.Activity
import com.example.poketouch.ControllerFragment

class StateManager(
    private val wasmBoy: WasmBoy,
    private val breakMan: BreakpointManager,
    private val controller: ControllerFragment,
    private val activity: Activity,
    ) {
    public enum class MainState {
        Battle,
        Overworld,
    }
    public enum class SubState {
        BattleWaiting,
        BattleChoosingAction,
        BattleChoosingMove,
    }

    private var mainState: MainState = MainState.Overworld
    private var subState: SubState? = null
    private var subSubState = -1

    init {
        breakMan.clearPCBreakPoints()
        breakMan.setPCBreakPoint(Offsets.StartBattle)
    }

    private fun setState() {
        val oldMainState = mainState
        val oldSubState = subState
        val oldSubSubState = subSubState

        val pc = wasmBoy.programCounter
        val bank = getRomBank()

        when {
            pc == Offsets.StartBattle -> {
                mainState = MainState.Battle
                subState = SubState.BattleWaiting
                subSubState = -1
           }
            pc == Offsets.ExitBattle -> {
                mainState = MainState.Overworld
                subState = null
                subSubState = -1
            }
            pc == Offsets.LoadBattleMenu -> {
                mainState = MainState.Battle
                subState = SubState.BattleChoosingAction
                subSubState = -1
            }
            pc == Offsets.ListMoves_after_read_name
                    && bank == Offsets.RomBankBattle
                    && subSubState < 4 -> {
                mainState = MainState.Battle
                subState = SubState.BattleChoosingMove
                subSubState += 1
            }
            subState == SubState.BattleChoosingMove && subSubState >= 4 -> {
                subState = SubState.BattleWaiting
                subSubState = -1
            }
            else -> {
//                mainState = MainState.Overworld
//                subState = null
            }
        }

        if (mainState != oldMainState || subState != oldSubState || subSubState != oldSubSubState) {
            println("### Setting new state: $mainState, $subState, $subSubState")
        }
    }

    public fun act() {
        val oldMainState = mainState
        setState()

        when (mainState) {
            MainState.Overworld -> {
//               if (oldMainState == mainState) return
//                println("## Overworld, setting breakpoints")
                activity.runOnUiThread {
                    controller.buttonAdapter.clearOptions()
                }
                breakMan.clearPCBreakPoints()
                breakMan.setPCBreakPoint(Offsets.StartBattle)
            }
            MainState.Battle -> {
//                if (oldMainState == mainState) return
//                println("## Battle, setting breakpoints")
                breakMan.clearPCBreakPoints()
                breakMan.setPCBreakPoint(Offsets.ExitBattle)
                breakMan.setPCBreakPoint(Offsets.LoadBattleMenu)
                breakMan.setPCBreakPoint(Offsets.ListMoves_after_read_name)
                handleBattle()
            }
            else -> {
                println("#### Well fuck me I guess")
            }
        }
    }

    private fun handleBattle() {
        when (subState) {
            SubState.BattleWaiting -> {
//               println("just waiting...")
                activity.runOnUiThread {
                    controller.buttonAdapter.clearOptions()
                }
            }
            SubState.BattleChoosingAction -> {
//               println("just waiting...")
                activity.runOnUiThread {
                    controller.buttonAdapter.clearOptions()
                }
            }
            SubState.BattleChoosingMove -> {
                val moveName = getString(Offsets.wStringBuffer1)

                activity.runOnUiThread {
                    if (subSubState === 0) {
                        controller.buttonAdapter.clearOptions()
                    }
                    controller.buttonAdapter.addOption(moveName)
                    println("## Move ${subSubState + 1}: $moveName")
                }

            }
        }
    }

    private fun getRomBank(): Int {
        val offset = wasmBoy.getWasmBoyOffsetFromGameBoyOffset(Offsets.hROMBank)
        return wasmBoy.memory.get(offset).toInt()
    }

    private fun getBytes(gameOffset: Int, numBytes: Int): ByteArray {
        val bytes = ByteArray(numBytes)
        val offset = wasmBoy.getWasmBoyOffsetFromGameBoyOffset(gameOffset)
        wasmBoy.memory.position(offset)
        wasmBoy.memory.get(bytes)
        return bytes
    }

    private fun getString(gameOffset: Int): String {
        val bytes = getBytes(gameOffset, 20)
        val ogString = Charmap.bytesToString(bytes)
        return ogString.split("@")[0]
    }
}
