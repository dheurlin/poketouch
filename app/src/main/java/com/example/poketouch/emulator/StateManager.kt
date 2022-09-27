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
        BattleActionChosen,
        BattleChoosingMove,
        BattleMoveChosen,
    }

    private var mainState: MainState = MainState.Overworld
    private var subState: SubState? = null
    private var menuOption: Int? = null

    init {
        breakMan.clearPCBreakPoints()
        breakMan.setPCBreakPoint(Offsets.StartBattle)
    }

    private fun setState() {
        val oldMainState = mainState
        val oldSubState = subState
        val oldSubSubState = menuOption

        val pc = wasmBoy.programCounter
        val bank = getRomBank()

        // TODO Outer condition on MainState?
        when {
            pc == Offsets.StartBattle -> {
                mainState = MainState.Battle
                subState = SubState.BattleWaiting
                menuOption = null
           }
            pc == Offsets.ExitBattle -> {
                mainState = MainState.Overworld
                subState = null
                menuOption = null
            }
            pc == Offsets.BattleMenu -> {
                mainState = MainState.Battle
                subState = SubState.BattleChoosingAction
                menuOption = null
            }
            pc == Offsets.BattleMenu_next && bank == Offsets.RomBankBattleMenu -> {
                mainState = MainState.Battle
                subState = SubState.BattleActionChosen
                // set by button press
                // menuOption = null
            }
            pc == Offsets.ListMoves && bank == Offsets.RomBankBattle -> {
                mainState = MainState.Battle
                subState = SubState.BattleChoosingMove
                menuOption = null
            }
            pc == Offsets.MoveSelectionScreen_use_move_not_b -> {
                mainState = MainState.Battle
                subState = SubState.BattleMoveChosen
                // subSubState has been set by buttonPress
            }
            subState == SubState.BattleMoveChosen -> {
                mainState = MainState.Battle
                subState = SubState.BattleWaiting
                menuOption = null
            }
            else -> {
//                mainState = MainState.Overworld
//                subState = null
            }
        }

        if (mainState != oldMainState || subState != oldSubState || menuOption != oldSubSubState) {
            val bankStr = "%02x".format(bank)
            val pcStr = "%04x".format(pc)
            println("### [\$$bankStr:$pcStr] Setting new state: $mainState, $subState, $menuOption")
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
                breakMan.setPCBreakPoint(Offsets.BattleMenu)
                breakMan.setPCBreakPoint(Offsets.BattleMenu_next)
                breakMan.setPCBreakPoint(Offsets.ListMoves)
                breakMan.setPCBreakPoint(Offsets.MoveSelectionScreen_use_move_not_b)
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
                    controller.releaseButtons()
                    listOf("FIGHT", "POKÉMON", "PACK", "RUN").forEach {
                        controller.buttonAdapter.addOption(it) { ix ->
                            menuOption = ix + 1 // 1 indexed
                            controller.aButton = true
                        }
                    }
                }
            }
            SubState.BattleActionChosen -> {
                activity.runOnUiThread { controller.buttonAdapter.clearOptions() }

                wasmBoy.memory.put(
                    wasmBoy.getWasmBoyOffsetFromGameBoyOffset(Offsets.wBattleMenuCursorPosition),
                    menuOption!!.toByte()
                )
            }
            SubState.BattleChoosingMove -> {
                val moveNums = getBytes(Offsets.wListMoves_MoveIndicesBuffer, 4)
                val moveNames = getMoveNames(moveNums)
                activity.runOnUiThread {
                    controller.buttonAdapter.clearOptions()
                    controller.releaseButtons()
                    for (s in moveNames) {
                        controller.buttonAdapter.addOption(s) {
                            menuOption = it
                            controller.aButton = true
                        }
                    }
                }
            }
            SubState.BattleMoveChosen -> {
                activity.runOnUiThread { controller.buttonAdapter.clearOptions() }

                wasmBoy.memory.put(
                    wasmBoy.getWasmBoyOffsetFromGameBoyOffset(Offsets.wMenuCursorY),
                    menuOption!!.toByte()
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

