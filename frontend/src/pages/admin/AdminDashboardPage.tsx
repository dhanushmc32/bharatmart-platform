import React, { useEffect, useState } from 'react';
import { api } from '../../api/axiosClient';
import { useStore } from '../../context/StoreContext';
import { StatusBadge } from '../../components/StatusBadge';

export const AdminDashboardPage: React.FC = () => {
  const { stores } = useStore();
  const [payments, setPayments] = useState<any[]>([]);

  useEffect(() => {
    api.get('/admin/payments').then((res) => setPayments(res.data.data || [])).catch(() => {});
  }, []);

  return (
    <div className="max-w-6xl mx-auto px-4 py-8 space-y-8">
      <h1 className="text-2xl font-bold text-slate-900">Admin Platform Overview</h1>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="bg-white p-5 rounded-xl border border-slate-200">
          <div className="text-sm text-slate-500">Active India Stores</div>
          <div className="text-3xl font-bold text-slate-900 mt-1">{stores.length}</div>
        </div>
        <div className="bg-white p-5 rounded-xl border border-slate-200">
          <div className="text-sm text-slate-500">COD Payment Records</div>
          <div className="text-3xl font-bold text-slate-900 mt-1">{payments.length}</div>
        </div>
        <div className="bg-white p-5 rounded-xl border border-slate-200">
          <div className="text-sm text-slate-500">Payment Mode Policy</div>
          <div className="text-xl font-bold text-amber-700 mt-2">COD Only</div>
        </div>
      </div>

      <div className="bg-white rounded-xl border border-slate-200 overflow-hidden">
        <div className="p-4 border-b border-slate-200 font-semibold">COD Payment Ledger</div>
        <table className="w-full text-sm text-left">
          <thead className="bg-slate-50 border-b border-slate-200 text-slate-600">
            <tr>
              <th className="p-3">Order ID</th>
              <th className="p-3">Method</th>
              <th className="p-3">Amount (INR)</th>
              <th className="p-3">Status</th>
              <th className="p-3">Collected By</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {payments.map((p) => (
              <tr key={p.id}>
                <td className="p-3">#{p.orderId}</td>
                <td className="p-3">{p.paymentMethod}</td>
                <td className="p-3 font-semibold">₹{p.amount}</td>
                <td className="p-3"><StatusBadge status={p.status} /></td>
                <td className="p-3">{p.collectedBy || '—'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};
