package com.gdut.fundraising.blockchain.Service;

import com.gdut.fundraising.blockchain.Pointer;
import com.gdut.fundraising.blockchain.UTXO;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public interface UTXOService {
    /**
     * 根据定位指针删除对应的utxo集合
     * @param utxoHashMap
     * @param pointerList
     * @return
     */
    ConcurrentHashMap<Pointer, UTXO> deleteUTXOByPointer(ConcurrentHashMap<Pointer, UTXO> utxoHashMap, List<Pointer> pointerList);

    void addUTXOToMap(ConcurrentHashMap<Pointer, UTXO>utxoHashMap, List<UTXO> utxoList);

}
