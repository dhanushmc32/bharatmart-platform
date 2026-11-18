import React, { useEffect, useState } from 'react';
import { api } from '../../api/axiosClient';
import { useStore } from '../../context/StoreContext';
import { StoreInventory } from '../../types';
import { StatusBadge } from '../../components/StatusBadge';

export const StoreDashboardPage: React.FC = () => {
  const { selectedStore } = useStore();
  const [inventory, setInventory] = useState<StoreInventory[]>([]);
  const [orderIdInput, setOrderIdInput] = useState('');
  const [codMessage, setCodMessage] = useState('');

  const loadInventory = () => {
    if (!selectedStore) return;
    api.get(`/stores/${selectedStore.storeCode}/inventory`).then((res) => setInventory(res.data.data || []));
  };

  useEffect(loadInventory, [selectedStore]);

  const restock = async (skuId: number, currentQty: number) => {
    if (!selectedStore) return;
    await api.put(`/stores/${selectedStore.storeCode}/inventory/${skuId}`, {
      availableQty: currentQty + 10,
      reason: 'RESTOCK',
    });
    loadInventory();
  };

  const markCodPaid = async (status: 'CASH_COLLECTED' | 'PAID') => {
    if (!orderIdInput) return;
    await api.patch(`/store/payments/${orderIdInput}/cod`, {
      status,
      collectedBy: 'Store Delivery Agent',
      remarks: `Updated to ${status}`,
    });
    setCodMessage(`Order #${orderIdInput} COD marked as ${status}`);
  };

  return (
    <div className="max-w-6xl mx-auto px-4 py-8 space-y-8">
      <h1 className="text-2xl font-bold text-slate-900">
        Store Manager Dashboard — {selectedStore?.storeCode}
      </h1>

      <div className="bg-white rounded-xl border border-slate-200 p-6">
        <h2 className="text-lg font-semibold mb-4">COD Cash Collection Update</h2>
        <div className="flex flex-wrap gap-3 items-center">
          <input
            type="number"
            placeholder="Order ID (e.g. 1)"
            value={orderIdInput}
            onChange={(e) => setOrderIdInput(e.target.value)}
            className="border border-slate-300 rounded-lg px-3 py-2 text-sm"
          />
          <button onClick={() => markCodPaid('CASH_COLLECTED')} className="px-4 py-2 rounded-lg bg-blue-600 text-white text-sm font-medium">
            Mark CASH_COLLECTED
          </button>
          <button onClick={() => markCodPaid('PAID')} className="px-4 py-2 rounded-lg bg-emerald-600 text-white text-sm font-medium">
            Mark PAID (Settle & Emit XML)
          </button>
          {codMessage && <span className="text-sm text-emerald-700 font-medium">{codMessage}</span>}
        </div>
      </div>

      <div className="bg-white rounded-xl border border-slate-200 overflow-hidden">
        <table className="w-full text-sm text-left">
          <thead className="bg-slate-50 border-b border-slate-200 text-slate-600">
            <tr>
              <th className="p-3">SKU</th>
              <th className="p-3">Product</th>
              <th className="p-3">Available</th>
              <th className="p-3">Reserved</th>
              <th className="p-3">Alert</th>
              <th className="p-3">Action</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {inventory.map((inv) => (
              <tr key={inv.inventoryId}>
                <td className="p-3 font-mono text-xs">{inv.skuCode}</td>
                <td className="p-3">{inv.productName} ({inv.variantName})</td>
                <td className="p-3 font-semibold">{inv.availableQty}</td>
                <td className="p-3">{inv.reservedQty}</td>
                <td className="p-3">
                  {inv.lowStock ? <StatusBadge status="FAILED" /> : <StatusBadge status="SUCCESS" />}
                </td>
                <td className="p-3">
                  <button
                    onClick={() => restock(inv.skuId, inv.availableQty)}
                    className="px-3 py-1 rounded bg-indigo-50 text-indigo-700 text-xs font-semibold hover:bg-indigo-100"
                  >
                    +10 Stock
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};
