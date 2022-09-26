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
        BattleUsingMove,
    }

    private var mainState: MainState = MainState.Overworld
    private var subState: SubState? = null
    private var subSubState: Int? = null

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
                subSubState = null
           }
            pc == Offsets.ExitBattle -> {
                mainState = MainState.Overworld
                subState = null
                subSubState = null
            }
            pc == Offsets.LoadBattleMenu -> {
                mainState = MainState.Battle
                subState = SubState.BattleChoosingAction
                subSubState = null
            }
            pc == Offsets.ListMoves && bank == Offsets.RomBankBattle -> {
                mainState = MainState.Battle
                subState = SubState.BattleChoosingMove
                subSubState = null
            }
            pc == Offsets.MoveSelectionScreen_use_move -> {
                mainState = MainState.Battle
                subState = SubState.BattleUsingMove
                // subSubState has been set by buttonPress
            }
//            subState == SubState.BattleUsingMove -> {
//                mainState = MainState.Battle
//                subState = SubState.BattleWaiting
//                subSubState = null
//            }
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
                activity.runOnUiThread {
                    controller.buttonAdapter.clearOptions()
                }
                breakMan.clearPCBreakPoints()
                breakMan.setPCBreakPoint(Offsets.StartBattle)
            }
            MainState.Battle -> {
                breakMan.clearPCBreakPoints()
                breakMan.setPCBreakPoint(Offsets.ExitBattle)
                breakMan.setPCBreakPoint(Offsets.LoadBattleMenu)
                breakMan.setPCBreakPoint(Offsets.ListMoves)
                breakMan.setPCBreakPoint(Offsets.MoveSelectionScreen_use_move)
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
                activity.runOnUiThread {
                    controller.buttonAdapter.clearOptions()
                }
            }
            // BUG: App crashes when pressing back here!
            SubState.BattleChoosingMove -> {
                val moveNums = getBytes(Offsets.wListMoves_MoveIndicesBuffer, 4)
                val moveNames = getMoveNames(moveNums)
                activity.runOnUiThread {
                    controller.buttonAdapter.clearOptions()
                    controller.releaseButtons()
                    for (s in moveNames) {
                        controller.buttonAdapter.addOption(s) {
                            subSubState = it
                            controller.aButton = true
                        }
                    }
                }
            }
            SubState.BattleUsingMove -> {
                wasmBoy.memory.put(
                    wasmBoy.getWasmBoyOffsetFromGameBoyOffset(Offsets.wMenuCursorY),
                    subSubState!!.toByte()
                )
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

    private fun getBytesFromBank(bank: Int, gbOffset: Int, numBytes: Int): ByteArray {
        val bytes = ByteArray(numBytes)
        val romBankOffset =  (0x4000 * bank + (gbOffset - Offsets.WasmBoySwitchableCartridgeRomLocation));
        val offset = romBankOffset + wasmBoy.cartridgE_ROM_LOCATION
        wasmBoy.memory.position(offset)
        wasmBoy.memory.get(bytes)
        return bytes
    }

    private fun getMoveNames(bs: ByteArray): List<String> {
        val bytes = getBytesFromBank(
            Offsets.RomBankNames,
            Offsets.MoveNames,
            Offsets.MoveNameLength * Offsets.NumMoves
        )

        val allStrings = Charmap.bytesToString(bytes).split("@")
        return bs.map {
            val i = it.toInt()
            if (i > 0) allStrings[it.toInt() - 1] else null
        }.filterNotNull()
    }
}

