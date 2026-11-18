import React from 'react';

const COLORS: Record<string, string> = {
  SUCCESS: 'bg-emerald-100 text-emerald-800',
  CONFIRMED: 'bg-emerald-100 text-emerald-800',
  PAID: 'bg-emerald-100 text-emerald-800',
  DELIVERED: 'bg-emerald-100 text-emerald-800',
  PENDING: 'bg-amber-100 text-amber-800',
  PROCESSING: 'bg-blue-100 text-blue-800',
  CASH_COLLECTED: 'bg-blue-100 text-blue-800',
  RETRYING: 'bg-orange-100 text-orange-800',
  FAILED: 'bg-rose-100 text-rose-800',
  DEAD_LETTER: 'bg-rose-200 text-rose-900',
  CANCELLED: 'bg-slate-200 text-slate-700',
};

export const StatusBadge: React.FC<{ status: string }> = ({ status }) => {
  const cls = COLORS[status] || 'bg-slate-100 text-slate-800';
  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold ${cls}`}>
      {status}
    </span>
  );
};
