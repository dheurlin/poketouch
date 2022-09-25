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

//    $4d74 = ListMoves.moves_loop
//    $4da7 = ListMoves.no_more_moves
//    $4da8 = ListMoves.nonmove_loop
//    $4db8 = ListMoves.done

    // Values
    const val wMenuData_2DMenuItemStringsBank = 0xcf94
    const val wMenuData_2DMenuItemStringsAddr = 0xcf95
    const val wStringBuffer1 = 0xd073
}