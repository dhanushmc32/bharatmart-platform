import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../../api/axiosClient';
import { useStore } from '../../context/StoreContext';
import { Cart } from '../../types';

export const CartCheckoutPage: React.FC = () => {
  const { selectedStore } = useStore();
  const navigate = useNavigate();
  const [cart, setCart] = useState<Cart | null>(null);
  const [address, setAddress] = useState('42, 12th Main Road, Indiranagar, Bengaluru 560008');
  const [error, setError] = useState('');

  const loadCart = () => {
    if (!selectedStore) return;
    api.get('/cart', { params: { storeCode: selectedStore.storeCode } })
      .then((res) => setCart(res.data.data))
      .catch(() => {});
  };

  useEffect(loadCart, [selectedStore]);

  const removeItem = async (skuId: number) => {
    if (!selectedStore) return;
    await api.delete(`/cart/items/${skuId}`, { params: { storeCode: selectedStore.storeCode } });
    loadCart();
  };

  const placeCodOrder = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedStore) return;
    setError('');
    try {
      await api.post('/orders', {
        storeCode: selectedStore.storeCode,
        shippingAddress: address,
        paymentMethod: 'COD',
      });
      navigate('/orders');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Checkout failed');
    }
  };

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-slate-900 mb-6">Shopping Cart & COD Checkout</h1>
      {error && <div className="mb-4 p-3 rounded-lg bg-rose-50 text-rose-700 text-sm">{error}</div>}

      {!cart || cart.items.length === 0 ? (
        <div className="bg-white p-8 rounded-xl border border-slate-200 text-center text-slate-600">
          Your cart for {selectedStore?.storeCode} is empty.
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="md:col-span-2 bg-white rounded-xl border border-slate-200 p-6 space-y-4">
            {cart.items.map((item) => (
              <div key={item.itemId} className="flex items-center justify-between border-b border-slate-100 pb-3">
                <div>
                  <div className="font-semibold text-slate-900">{item.productName}</div>
                  <div className="text-xs text-slate-500">{item.variantName} • Qty: {item.quantity}</div>
                </div>
                <div className="flex items-center gap-4">
                  <span className="font-semibold">₹{item.lineTotal}</span>
                  <button onClick={() => removeItem(item.skuId)} className="text-xs text-rose-600 hover:underline">
                    Remove
                  </button>
                </div>
              </div>
            ))}
          </div>

          <form onSubmit={placeCodOrder} className="bg-white rounded-xl border border-slate-200 p-6 space-y-4 h-fit">
            <div className="flex justify-between font-bold text-lg">
              <span>Total</span>
              <span>₹{cart.subtotal}</span>
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700">Delivery Address</label>
              <textarea
                required
                rows={3}
                value={address}
                onChange={(e) => setAddress(e.target.value)}
                className="mt-1 w-full border border-slate-300 rounded-lg p-2 text-sm"
              />
            </div>
            <div className="p-3 rounded-lg bg-amber-50 border border-amber-200 text-xs text-amber-900">
              <strong>Payment Method:</strong> Cash on Delivery (COD) Only — pay cash to the delivery agent upon arrival.
            </div>
            <button type="submit" className="w-full py-2.5 rounded-lg bg-indigo-600 text-white font-semibold hover:bg-indigo-700">
              Place COD Order
            </button>
          </form>
        </div>
      )}
    </div>
  );
};
