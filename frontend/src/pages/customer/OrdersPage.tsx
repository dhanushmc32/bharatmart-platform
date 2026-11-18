import React, { useEffect, useState } from 'react';
import { api } from '../../api/axiosClient';
import { Order } from '../../types';
import { StatusBadge } from '../../components/StatusBadge';

export const OrdersPage: React.FC = () => {
  const [orders, setOrders] = useState<Order[]>([]);

  const loadOrders = () => {
    api.get('/orders').then((res) => setOrders(res.data.data || []));
  };

  useEffect(loadOrders, []);

  const cancelOrder = async (orderNumber: string) => {
    await api.post(`/orders/${orderNumber}/cancel`);
    loadOrders();
  };

  return (
    <div className="max-w-5xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-slate-900 mb-6">My Orders & Tracking</h1>
      <div className="space-y-4">
        {orders.map((o) => (
          <div key={o.id} className="bg-white rounded-xl border border-slate-200 p-6">
            <div className="flex flex-wrap items-center justify-between gap-4 border-b border-slate-100 pb-4">
              <div>
                <div className="font-bold text-slate-900">{o.orderNumber}</div>
                <div className="text-xs text-slate-500">
                  Store: {o.storeCode} • Correlation: {o.correlationId}
                </div>
              </div>
              <div className="flex items-center gap-2">
                <StatusBadge status={o.status} />
                <StatusBadge status={`COD: ${o.paymentStatus}`} />
                {o.status !== 'CANCELLED' && o.status !== 'DELIVERED' && (
                  <button
                    onClick={() => cancelOrder(o.orderNumber)}
                    className="ml-2 px-3 py-1 rounded-lg border border-rose-300 text-rose-700 text-xs font-medium hover:bg-rose-50"
                  >
                    Cancel Order
                  </button>
                )}
              </div>
            </div>
            <div className="mt-3 text-sm text-slate-700 space-y-1">
              {o.items.map((i, idx) => (
                <div key={idx} className="flex justify-between">
                  <span>{i.productName} ({i.skuCode}) × {i.quantity}</span>
                  <span>₹{i.lineTotal}</span>
                </div>
              ))}
              <div className="flex justify-between font-bold pt-2 border-t border-slate-100">
                <span>Total (COD)</span>
                <span>₹{o.totalAmount}</span>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
