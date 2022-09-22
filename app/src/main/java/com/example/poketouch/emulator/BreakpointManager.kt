package com.example.poketouch.emulator

import WasmBoy

class BreakpointManager(wasmBoy: WasmBoy) {
    private val wasmBoy: WasmBoy = wasmBoy
    private var numUsed = 0
    val breakpointsSet = ArrayList<Int>(10)

    public fun setPCBreakPoint(addr: Int) {
        when (numUsed) {
            0 -> wasmBoy.setProgramCounterBreakpoint0(addr)
            1 -> wasmBoy.setProgramCounterBreakpoint1(addr)
            2 -> wasmBoy.setProgramCounterBreakpoint2(addr)
            3 -> wasmBoy.setProgramCounterBreakpoint3(addr)
            4 -> wasmBoy.setProgramCounterBreakpoint4(addr)
            5 -> wasmBoy.setProgramCounterBreakpoint5(addr)
            6 -> wasmBoy.setProgramCounterBreakpoint6(addr)
            7 -> wasmBoy.setProgramCounterBreakpoint7(addr)
            8 -> wasmBoy.setProgramCounterBreakpoint8(addr)
            9 -> wasmBoy.setProgramCounterBreakpoint9(addr)
            else -> throw IndexOutOfBoundsException("All breakpoints already used!")
        }
        breakpointsSet.add(addr)
        numUsed += 1
    }
}