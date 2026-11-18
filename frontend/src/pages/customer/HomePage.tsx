import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Search } from 'lucide-react';
import { api } from '../../api/axiosClient';
import { useStore } from '../../context/StoreContext';
import { useAuth } from '../../context/AuthContext';
import { Product, StoreInventory } from '../../types';

export const HomePage: React.FC = () => {
  const { selectedStore } = useStore();
  const { user } = useAuth();
  const navigate = useNavigate();
  const [products, setProducts] = useState<Product[]>([]);
  const [inventoryMap, setInventoryMap] = useState<Record<number, StoreInventory>>({});
  const [query, setQuery] = useState('');
  const [toast, setToast] = useState('');

  useEffect(() => {
    api.get('/products', { params: query ? { q: query } : {} }).then((res) => {
      setProducts(res.data.data || []);
    });
  }, [query]);

  useEffect(() => {
    if (!selectedStore) return;
    api.get(`/stores/${selectedStore.storeCode}/inventory`).then((res) => {
      const map: Record<number, StoreInventory> = {};
      for (const item of res.data.data || []) {
        map[item.skuId] = item;
      }
      setInventoryMap(map);
    });
  }, [selectedStore]);

  const addToCart = async (skuId: number) => {
    if (!user) {
      navigate('/login');
      return;
    }
    if (!selectedStore) return;
    try {
      await api.post('/cart/items', { storeCode: selectedStore.storeCode, skuId, quantity: 1 });
      setToast('Added to cart!');
      setTimeout(() => setToast(''), 2500);
    } catch (err: any) {
      setToast(err.response?.data?.message || 'Could not add item');
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-8">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">
            Shopping from {selectedStore ? `${selectedStore.name} (${selectedStore.storeCode})` : 'India Stores'}
          </h1>
          <p className="text-sm text-slate-600 mt-1">Cash on Delivery (COD) available across India</p>
        </div>
        <div className="relative w-full md:w-80">
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
          <input
            type="text"
            placeholder="Search products..."
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            className="w-full pl-9 pr-4 py-2 border border-slate-300 rounded-lg bg-white"
          />
        </div>
      </div>

      {toast && (
        <div className="mb-6 p-3 rounded-lg bg-emerald-50 border border-emerald-200 text-emerald-800 text-sm">
          {toast}
        </div>
      )}

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {products.map((p) => (
          <div key={p.id} className="bg-white rounded-xl border border-slate-200 p-5 flex flex-col justify-between shadow-sm">
            <div>
              <span className="text-xs font-semibold text-indigo-600 uppercase">{p.categoryName}</span>
              <h2 className="text-lg font-bold text-slate-900 mt-1">{p.name}</h2>
              <p className="text-sm text-slate-600 mt-1">{p.description}</p>
            </div>
            <div className="mt-4 space-y-2 border-t border-slate-100 pt-4">
              {p.skus.map((sku) => {
                const inv = inventoryMap[sku.id];
                const inStock = inv && inv.availableQty > 0;
                return (
                  <div key={sku.id} className="flex items-center justify-between gap-2 text-sm">
                    <div>
                      <div className="font-medium text-slate-800">{sku.variantName}</div>
                      <div className="text-xs text-slate-500">
                        ₹{sku.sellingPrice} • {inv ? `${inv.availableQty} in stock` : 'Not stocked here'}
                      </div>
                    </div>
                    <button
                      disabled={!inStock}
                      onClick={() => addToCart(sku.id)}
                      className="px-3 py-1.5 rounded-lg bg-indigo-600 text-white text-xs font-semibold hover:bg-indigo-700 disabled:bg-slate-200 disabled:text-slate-400"
                    >
                      {inStock ? 'Add' : 'Out of Stock'}
                    </button>
                  </div>
                );
              })}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
