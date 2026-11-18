import React from 'react';
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { StoreProvider } from './context/StoreContext';
import { Navbar } from './components/Navbar';
import { HomePage } from './pages/customer/HomePage';
import { CartCheckoutPage } from './pages/customer/CartCheckoutPage';
import { OrdersPage } from './pages/customer/OrdersPage';
import { LoginPage } from './pages/auth/LoginPage';
import { RegisterPage } from './pages/auth/RegisterPage';
import { StoreDashboardPage } from './pages/store/StoreDashboardPage';
import { AdminDashboardPage } from './pages/admin/AdminDashboardPage';
import { MiddlewareMonitorPage } from './pages/middleware/MiddlewareMonitorPage';

const ProtectedRoute: React.FC<{ role?: string; children: React.ReactNode }> = ({ role, children }) => {
  const { user, hasRole } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  if (role && !hasRole(role) && !hasRole('ADMIN')) return <Navigate to="/" replace />;
  return <>{children}</>;
};

export default function App() {
  return (
    <AuthProvider>
      <StoreProvider>
        <BrowserRouter>
          <div className="min-h-screen bg-slate-50">
            <Navbar />
            <Routes>
              <Route path="/" element={<HomePage />} />
              <Route path="/login" element={<LoginPage />} />
              <Route path="/register" element={<RegisterPage />} />
              <Route path="/cart" element={<ProtectedRoute><CartCheckoutPage /></ProtectedRoute>} />
              <Route path="/orders" element={<ProtectedRoute><OrdersPage /></ProtectedRoute>} />
              <Route path="/store" element={<ProtectedRoute role="STORE_MANAGER"><StoreDashboardPage /></ProtectedRoute>} />
              <Route path="/admin" element={<ProtectedRoute role="ADMIN"><AdminDashboardPage /></ProtectedRoute>} />
              <Route path="/admin/middleware" element={<ProtectedRoute role="ADMIN"><MiddlewareMonitorPage /></ProtectedRoute>} />
            </Routes>
          </div>
        </BrowserRouter>
      </StoreProvider>
    </AuthProvider>
  );
}
