export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface AuthUser {
  userId: number;
  username: string;
  email: string;
  roles: string[];
  accessToken: string;
  refreshToken: string;
}

export interface Store {
  id: number;
  storeCode: string;
  name: string;
  stateCode: string;
  stateName: string;
  cityCode: string;
  cityName: string;
  addressLine: string;
  pincode: string;
  contactPhone: string;
  active: boolean;
}

export interface Sku {
  id: number;
  skuCode: string;
  variantName: string;
  mrp: number;
  sellingPrice: number;
}

export interface Product {
  id: number;
  name: string;
  slug: string;
  brand: string;
  categoryName: string;
  categorySlug: string;
  description: string;
  imageUrl?: string;
  skus: Sku[];
}

export interface StoreInventory {
  inventoryId: number;
  storeCode: string;
  skuId: number;
  skuCode: string;
  productName: string;
  variantName: string;
  sellingPrice: number;
  availableQty: number;
  reservedQty: number;
  reorderLevel: number;
  lowStock: boolean;
}

export interface CartItem {
  itemId: number;
  skuId: number;
  skuCode: string;
  productName: string;
  variantName: string;
  unitPrice: number;
  quantity: number;
  lineTotal: number;
}

export interface Cart {
  cartId: number | null;
  storeCode: string;
  items: CartItem[];
  subtotal: number;
}

export interface Order {
  id: number;
  orderNumber: string;
  storeCode: string;
  status: string;
  paymentMethod: 'COD';
  paymentStatus: 'PENDING' | 'CASH_COLLECTED' | 'PAID' | 'FAILED' | 'CANCELLED';
  totalAmount: number;
  shippingAddress: string;
  correlationId: string;
  createdAt: string;
  items: {
    skuCode: string;
    productName: string;
    quantity: number;
    unitPrice: number;
    lineTotal: number;
  }[];
}

export interface XmlMessage {
  id: number;
  messageId: string;
  correlationId: string;
  orderId: number | null;
  sourceSystem: string;
  destinationSystem: string;
  messageType: string;
  status: string;
  retryCount: number;
  requestXml: string;
  responseXml: string | null;
  errorDetails: string | null;
  createdAt: string;
}

export interface MessageHistory {
  id: number;
  xmlMessageId: number;
  status: string;
  attemptNumber: number;
  notes: string;
  loggedAt: string;
}
