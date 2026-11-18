import React, { useEffect, useState } from 'react';
import { RefreshCw } from 'lucide-react';
import { api } from '../../api/axiosClient';
import { MessageHistory, XmlMessage } from '../../types';
import { StatusBadge } from '../../components/StatusBadge';

export const MiddlewareMonitorPage: React.FC = () => {
  const [messages, setMessages] = useState<XmlMessage[]>([]);
  const [statusFilter, setStatusFilter] = useState('');
  const [query, setQuery] = useState('');
  const [selected, setSelected] = useState<XmlMessage | null>(null);
  const [history, setHistory] = useState<MessageHistory[]>([]);

  const loadMessages = () => {
    api.get('/admin/middleware/messages', {
      params: { status: statusFilter || undefined, q: query || undefined },
    }).then((res) => setMessages(res.data.data || []));
  };

  useEffect(loadMessages, [statusFilter]);

  const inspectMessage = async (msg: XmlMessage) => {
    setSelected(msg);
    const res = await api.get(`/admin/middleware/messages/${msg.id}/history`);
    setHistory(res.data.data || []);
  };

  const retryMessage = async (id: number) => {
    await api.post(`/admin/middleware/messages/${id}/retry`);
    loadMessages();
  };

  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      <div className="flex flex-wrap items-center justify-between gap-4 mb-6">
        <h1 className="text-2xl font-bold text-slate-900">Apache Camel XML Middleware Monitor</h1>
        <div className="flex gap-2">
          <input
            type="text"
            placeholder="Message or Correlation ID..."
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            className="border border-slate-300 rounded-lg px-3 py-1.5 text-sm"
          />
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="border border-slate-300 rounded-lg px-3 py-1.5 text-sm"
          >
            <option value="">All Statuses</option>
            {['CREATED', 'PROCESSING', 'SUCCESS', 'FAILED', 'RETRYING', 'DEAD_LETTER'].map((s) => (
              <option key={s} value={s}>{s}</option>
            ))}
          </select>
          <button onClick={loadMessages} className="px-3 py-1.5 rounded-lg bg-indigo-600 text-white text-sm flex items-center gap-1">
            <RefreshCw className="w-4 h-4" /> Search
          </button>
        </div>
      </div>

      <div className="bg-white rounded-xl border border-slate-200 overflow-x-auto">
        <table className="w-full text-sm text-left">
          <thead className="bg-slate-50 border-b border-slate-200 text-slate-600">
            <tr>
              <th className="p-3">Message ID</th>
              <th className="p-3">Correlation ID</th>
              <th className="p-3">Route</th>
              <th className="p-3">Type</th>
              <th className="p-3">Status</th>
              <th className="p-3">Retries</th>
              <th className="p-3">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {messages.map((m) => (
              <tr key={m.id} className="hover:bg-slate-50">
                <td className="p-3 font-mono text-xs">{m.messageId}</td>
                <td className="p-3 font-mono text-xs">{m.correlationId}</td>
                <td className="p-3 text-xs">{m.sourceSystem} → {m.destinationSystem}</td>
                <td className="p-3 font-semibold text-xs">{m.messageType}</td>
                <td className="p-3"><StatusBadge status={m.status} /></td>
                <td className="p-3">{m.retryCount}</td>
                <td className="p-3 flex gap-2">
                  <button onClick={() => inspectMessage(m)} className="text-indigo-600 text-xs font-semibold hover:underline">
                    Inspect XML
                  </button>
                  {(m.status === 'FAILED' || m.status === 'DEAD_LETTER') && (
                    <button onClick={() => retryMessage(m.id)} className="text-amber-700 text-xs font-semibold hover:underline">
                      Retry
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {selected && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-xl max-w-4xl w-full max-h-[85vh] overflow-y-auto p-6 space-y-4">
            <div className="flex justify-between items-center">
              <h2 className="text-lg font-bold">XML Payload & Audit Trail ({selected.messageId})</h2>
              <button onClick={() => setSelected(null)} className="text-sm text-slate-500 hover:text-slate-900">Close</button>
            </div>
            <div>
              <h3 className="text-xs font-semibold uppercase text-slate-500 mb-1">Request XML</h3>
              <pre className="bg-slate-900 text-emerald-300 p-3 rounded-lg text-xs overflow-x-auto">{selected.requestXml}</pre>
            </div>
            {selected.responseXml && (
              <div>
                <h3 className="text-xs font-semibold uppercase text-slate-500 mb-1">Acknowledgement / Response XML</h3>
                <pre className="bg-slate-900 text-sky-300 p-3 rounded-lg text-xs overflow-x-auto">{selected.responseXml}</pre>
              </div>
            )}
            <div>
              <h3 className="text-xs font-semibold uppercase text-slate-500 mb-1">Processing History</h3>
              <ul className="text-xs space-y-1">
                {history.map((h) => (
                  <li key={h.id} className="p-2 bg-slate-50 rounded border border-slate-200">
                    <strong>{h.status}</strong> (Attempt {h.attemptNumber}) — {h.notes}
                  </li>
                ))}
              </ul>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
