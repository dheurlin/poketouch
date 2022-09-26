package com.example.poketouch.emulator

object Offsets {
    // Functions
    const val StartBattle = 0x74c1
    const val ExitBattle = 0x769e
    const val Load2DMenuData = 0x1bb1
    const val LoadBattleMenu = 0x4ef2
    const val MoveSelectionScreen = 0x64bc
    const val MoveSelectionScreen_use_move = 0x65d9
    const val MoveSelectionScreen_battle_player_moves = 0x658e
    const val ListMoves_moves_loop = 0x4d74
    const val ListMoves_after_read_name = 0x4d87
    const val ListMoves = 0x4d6f

//    $4d74 = ListMoves.moves_loop
//    $4da7 = ListMoves.no_more_moves
//    $4da8 = ListMoves.nonmove_loop
//    $4db8 = ListMoves.done

    // Values
    const val wStringBuffer1 = 0xd073
    const val wListMoves_MoveIndicesBuffer = 0xd25e
    const val hROMBank = 0xff9d
    const val MoveNames = 0x5f29
    const val MoveNameLength = 13
    const val NumMoves = 251

    // ROM banks
    const val RomBankBattle = 0x14
    const val RomBankNames = 0x72

    // WasmBoy memory offsets
    const val WasmBoySwitchableCartridgeRomLocation = 0x4000
}