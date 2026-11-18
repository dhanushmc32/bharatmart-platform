import React, { createContext, useContext, useEffect, useState } from 'react';
import { api } from '../api/axiosClient';
import { Store } from '../types';

interface StoreContextValue {
  stores: Store[];
  selectedStore: Store | null;
  setSelectedStore: (store: Store) => void;
}

const StoreContext = createContext<StoreContextValue | null>(null);

export const StoreProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [stores, setStores] = useState<Store[]>([]);
  const [selectedStore, setSelectedStore] = useState<Store | null>(null);

  useEffect(() => {
    api.get('/stores').then((res) => {
      const list: Store[] = res.data.data || [];
      setStores(list);
      if (list.length > 0 && !selectedStore) {
        setSelectedStore(list[0]);
      }
    }).catch(() => {});
  }, []);

  return (
    <StoreContext.Provider value={{ stores, selectedStore, setSelectedStore }}>
      {children}
    </StoreContext.Provider>
  );
};

export const useStore = () => {
  const ctx = useContext(StoreContext);
  if (!ctx) throw new Error('useStore must be used inside StoreProvider');
  return ctx;
};
