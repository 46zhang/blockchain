package com.gdut.fundraising.blockchain.Service;

import com.gdut.fundraising.blockchain.Pointer;
import com.gdut.fundraising.blockchain.UTXO;

import java.util.HashMap;
import java.util.List;

public interface UTXOService {
    /**
     * 根据定位指针删除对应的utxo集合
     * @param utxoHashMap
     * @param pointerList
     * @return
     */
    HashMap<Pointer, UTXO> deleteUTXOByPointer(HashMap<Pointer, UTXO> utxoHashMap, List<Pointer> pointerList);

    void addUTXOToMap(HashMap<Pointer,UTXO>utxoHashMap,List<UTXO> utxoList);

}
