import React from 'react';
import { Link } from 'react-router-dom';
import { ShoppingCart, Store as StoreIcon, Shield, Activity, LogOut } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useStore } from '../context/StoreContext';

export const Navbar: React.FC = () => {
  const { user, logout, hasRole } = useAuth();
  const { stores, selectedStore, setSelectedStore } = useStore();

  return (
    <header className="bg-white border-b border-slate-200 sticky top-0 z-30">
      <div className="max-w-7xl mx-auto px-4 h-16 flex items-center justify-between gap-4">
        <div className="flex items-center gap-6">
          <Link to="/" className="text-xl font-bold text-indigo-700 tracking-tight">
            BharatMart
          </Link>
          <div className="hidden md:flex items-center gap-2 text-sm">
            <StoreIcon className="w-4 h-4 text-slate-500" />
            <select
              value={selectedStore?.storeCode || ''}
              onChange={(e) => {
                const found = stores.find((s) => s.storeCode === e.target.value);
                if (found) setSelectedStore(found);
              }}
              className="border border-slate-300 rounded-lg px-2.5 py-1.5 text-sm bg-slate-50"
            >
              {stores.map((s) => (
                <option key={s.storeCode} value={s.storeCode}>
                  {s.cityName} ({s.storeCode})
                </option>
              ))}
            </select>
          </div>
        </div>

        <nav className="flex items-center gap-4 text-sm font-medium">
          <Link to="/cart" className="flex items-center gap-1 text-slate-700 hover:text-indigo-600">
            <ShoppingCart className="w-4 h-4" /> Cart
          </Link>
          {user && (
            <Link to="/orders" className="text-slate-700 hover:text-indigo-600">
              My Orders
            </Link>
          )}
          {(hasRole('STORE_MANAGER') || hasRole('ADMIN')) && (
            <Link to="/store" className="text-slate-700 hover:text-indigo-600">
              Store Ops
            </Link>
          )}
          {hasRole('ADMIN') && (
            <>
              <Link to="/admin" className="flex items-center gap-1 text-slate-700 hover:text-indigo-600">
                <Shield className="w-4 h-4" /> Admin
              </Link>
              <Link to="/admin/middleware" className="flex items-center gap-1 text-slate-700 hover:text-indigo-600">
                <Activity className="w-4 h-4" /> XML Monitor
              </Link>
            </>
          )}

          {user ? (
            <button
              onClick={logout}
              className="flex items-center gap-1 px-3 py-1.5 rounded-lg bg-slate-100 hover:bg-slate-200 text-slate-700"
            >
              <LogOut className="w-4 h-4" /> {user.username}
            </button>
          ) : (
            <Link
              to="/login"
              className="px-4 py-1.5 rounded-lg bg-indigo-600 text-white hover:bg-indigo-700"
            >
              Sign In
            </Link>
          )}
        </nav>
      </div>
    </header>
  );
};
