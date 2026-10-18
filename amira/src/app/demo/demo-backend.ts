/**
 * In-browser stand-in for the Spring Boot API, used only by the "demo" build (see environment.demo.ts).
 * It follows the same rules as amiraserver: stock is reserved when an order is placed, deducted when it is
 * paid, restored on cancel; only Active/LowStock variants can be sold; owner-only endpoints return 403.
 * State lives in localStorage so a visit survives a refresh. Nothing is sent to a server.
 */

export interface DemoResult {
  status: number;
  body: unknown;
}

class ApiError extends Error {
  constructor(public status: number, message: string) {
    super(message);
  }
}

interface ProductRec { id: number; name: string; brand: string; description: string; category: string; imageUrl: string | null; dateAdded: string }
interface VariantRec {
  id: number; productId: number; size: string; sku: string; price: number; costPrice: number | null;
  stockQuantity: number; reservedQuantity: number; batchNumber: string;
  manufactureDate: string | null; expiryDate: string | null; discontinued: boolean;
}
interface CustomerRec { id: number; name: string; email: string; phone?: string; shippingAddress?: string; role: 'CUSTOMER' | 'OWNER'; password: string }
interface ItemRec { id: number; orderId: number; variantId: number; productName: string; size: string; quantity: number; priceAtPurchase: number; subtotal: number }
interface OrderRec {
  id: number; description?: string; customerId: number; customerName: string; shippingAddress: string;
  dateOrdered: string; totalAmount: number; statusName: string; paymentStatus: string; items: ItemRec[];
}
interface PaymentRec { id: number; orderId: number; method: string; amount: number; paymentDate: string; status: string }
interface State {
  seq: { product: number; variant: number; customer: number; order: number; item: number; payment: number };
  products: ProductRec[]; variants: VariantRec[]; customers: CustomerRec[]; orders: OrderRec[]; payments: PaymentRec[];
}

const STORAGE_KEY = 'amira.demo.state.v1';
const LOW_STOCK_THRESHOLD = 5;
const OWNER_EMAIL = 'owner@amira.ph';
const OWNER_PASSWORD = 'owner123';

// name, brand, category, photo in assets/products, 50ml price in PHP (same list as the backend DataSeeder)
const CATALOG: [string, string, string, string, number][] = [
  ['Sauvage', 'Dior', 'Men', 'sauvage.jpg', 5200],
  ["J'adore", 'Dior', 'Women', 'jadore.png', 5600],
  ['Miss Dior Eau de Parfum', 'Dior', 'Women', 'miss-dior-edp.jpg', 5400],
  ['Bleu de Chanel Eau de Parfum', 'Chanel', 'Men', 'bleu-de-chanel-edp.jpg', 6200],
  ['Chanel No. 5 Eau de Parfum', 'Chanel', 'Women', 'chanel-no5-edp.jpg', 6600],
  ['Acqua di Gio', 'Giorgio Armani', 'Men', 'acqua-di-gio.jpg', 3900],
  ['Black Opium', 'Yves Saint Laurent', 'Women', 'black-opium.png', 4800],
  ['Libre', 'Yves Saint Laurent', 'Women', 'libre.jpg', 4900],
  ['Eros', 'Versace', 'Men', 'eros.png', 3400],
  ['Good Girl', 'Carolina Herrera', 'Women', 'good-girl.jpg', 5100],
  ['1 Million', 'Paco Rabanne', 'Men', '1-million.png', 3800],
  ['Invictus', 'Paco Rabanne', 'Men', 'invictus.jpg', 3700],
  ['La Vie Est Belle', 'Lancome', 'Women', 'la-vie-est-belle.jpg', 4700],
  ['Born in Roma Donna', 'Valentino', 'Women', 'donna-born-in-roma.jpg', 5000],
  ['Daisy Wild', 'Marc Jacobs', 'Women', 'daisy-wild.jpg', 3600],
  ['Paradoxe', 'Prada', 'Women', 'paradoxe.jpg', 5300],
  ['Polo Blue', 'Ralph Lauren', 'Men', 'polo-blue.jpg', 3300],
  ['Only The Brave', 'Diesel', 'Men', 'only-the-brave.jpg', 2900],
  ['CK One', 'Calvin Klein', 'Unisex', 'ck-one.jpg', 2600]
];

// ---------- dates ----------
const pad = (n: number) => String(n).padStart(2, '0');
const dateStr = (d: Date) => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
const dateTimeStr = (d: Date) => `${dateStr(d)}T${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
const today = () => dateStr(new Date());
const addDays = (d: Date, n: number) => new Date(d.getFullYear(), d.getMonth(), d.getDate() + n, d.getHours(), d.getMinutes());
const addMonths = (d: Date, n: number) => new Date(d.getFullYear(), d.getMonth() + n, d.getDate());

// ---------- state ----------
// loaded on the first request, after the module's helpers are defined
let state!: State;
let ready = false;

function load(): State {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (raw) {
      return JSON.parse(raw) as State;
    }
  } catch {
    /* storage blocked: start fresh, kept in memory only */
  }
  return seed();
}

function save(): void {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
  } catch {
    /* in-memory only */
  }
}

function seed(): State {
  const now = new Date();
  state = { seq: { product: 0, variant: 0, customer: 0, order: 0, item: 0, payment: 0 }, products: [], variants: [], customers: [], orders: [], payments: [] };

  state.customers.push({ id: ++state.seq.customer, name: 'AMIRA Owner', email: OWNER_EMAIL, role: 'OWNER', password: OWNER_PASSWORD });
  state.customers.push({
    id: ++state.seq.customer, name: 'Demo Customer', email: 'customer@amira.ph', role: 'CUSTOMER', password: 'customer123',
    phone: '09171234567', shippingAddress: '123 Sampaguita St, Makati City, Metro Manila'
  });

  for (const [name, brand, category, photo, price50] of CATALOG) {
    const p: ProductRec = {
      id: ++state.seq.product, name, brand, category,
      description: `${brand} ${name}, a ${category.toLowerCase()} fragrance.`,
      imageUrl: `assets/products/${photo}`, dateAdded: dateStr(now)
    };
    state.products.push(p);
    for (const [size, price, stock] of [['50ml', price50, 20], ['100ml', Math.round(price50 * 1.6), 12]] as [string, number, number][]) {
      state.variants.push({
        id: ++state.seq.variant, productId: p.id, size, sku: `AM-${p.id}-${size.toUpperCase()}`, price,
        costPrice: Math.round(price * 0.6 * 100) / 100, stockQuantity: stock, reservedQuantity: 0,
        batchNumber: `B-${now.getFullYear()}-01`, manufactureDate: dateStr(addMonths(now, -2)),
        expiryDate: dateStr(addMonths(now, 36)), discontinued: false
      });
    }
  }

  // a little sales history so the owner's reports have something to show
  const history: [number, [number, number][], number][] = [
    [18, [[1, 2], [9, 1]], 0], [14, [[3, 1], [15, 1]], 1], [11, [[5, 1]], 0],
    [8, [[1, 1], [13, 2]], 1], [5, [[7, 1], [2, 1]], 0], [3, [[21, 1]], 1]
  ];
  history.forEach(([daysAgo, lines, method], i) => {
    const customer = state.customers[1];
    const order = placeOrder(customer, lines.map(([variantId, quantity]) => ({ variantId, quantity })), customer.shippingAddress!);
    payOrder(order.id, method === 0 ? 'GCash' : 'Card');
    const when = addDays(now, -daysAgo);
    order.dateOrdered = dateTimeStr(when);
    state.payments.filter(p => p.orderId === order.id).forEach(p => (p.paymentDate = dateTimeStr(when)));
    if (i < 4) {
      setOrderStatus(order.id, 'Shipped');
      setOrderStatus(order.id, 'Delivered');
    }
  });
  save();
  return state;
}

// ---------- rules (mirror of Rules.java and the service impls) ----------
const available = (v: VariantRec) => v.stockQuantity - v.reservedQuantity;

function variantStatus(v: VariantRec): string {
  if (v.discontinued) {
    return 'Discontinued';
  }
  if (v.expiryDate && v.expiryDate < today()) {
    return 'Expired';
  }
  return available(v) <= LOW_STOCK_THRESHOLD ? 'LowStock' : 'Active';
}

const sellable = (v: VariantRec) => ['Active', 'LowStock'].includes(variantStatus(v));

function variantView(v: VariantRec, owner: boolean) {
  const p = state.products.find(x => x.id === v.productId);
  const { discontinued, ...rest } = v;
  const view: Record<string, unknown> = {
    ...rest, productName: p?.name ?? '', availableQuantity: available(v), status: variantStatus(v)
  };
  if (!owner) {
    delete view['costPrice'];
  }
  return view;
}

function productView(p: ProductRec, owner: boolean) {
  return { ...p, variants: state.variants.filter(v => v.productId === p.id).map(v => variantView(v, owner)) };
}

function findVariant(id: number): VariantRec {
  const v = state.variants.find(x => x.id === id);
  if (!v) {
    throw new ApiError(404, `Variant ${id} not found`);
  }
  return v;
}

function findOrder(id: number): OrderRec {
  const o = state.orders.find(x => x.id === id);
  if (!o) {
    throw new ApiError(404, `Order ${id} not found`);
  }
  return o;
}

function findPayment(id: number): PaymentRec {
  const p = state.payments.find(x => x.id === id);
  if (!p) {
    throw new ApiError(404, `Payment ${id} not found`);
  }
  return p;
}

function describe(v: VariantRec): string {
  return `${state.products.find(p => p.id === v.productId)?.name ?? 'variant'} ${v.size}`;
}

function placeOrder(customer: CustomerRec, items: { variantId: number; quantity: number }[], shippingAddress: string): OrderRec {
  if (!items?.length) {
    throw new ApiError(400, 'An order needs at least one item');
  }
  const address = shippingAddress?.trim() || customer.shippingAddress || '';
  if (!address) {
    throw new ApiError(400, 'A shipping address is required');
  }
  const wanted = new Map<number, number>();
  for (const item of items) {
    if (!(item.quantity > 0)) {
      throw new ApiError(400, 'Quantity must be at least 1');
    }
    wanted.set(item.variantId, (wanted.get(item.variantId) ?? 0) + item.quantity);
  }
  // check everything first so a failing line leaves no stock reserved
  const lines = [...wanted].map(([variantId, quantity]) => {
    const v = findVariant(variantId);
    if (!sellable(v) || available(v) < quantity) {
      throw new ApiError(400, `Not enough stock for ${describe(v)}`);
    }
    return { v, quantity };
  });
  const orderId = ++state.seq.order;
  const orderItems: ItemRec[] = lines.map(({ v, quantity }) => {
    v.reservedQuantity += quantity;
    return {
      id: ++state.seq.item, orderId, variantId: v.id, productName: state.products.find(p => p.id === v.productId)?.name ?? '',
      size: v.size, quantity, priceAtPurchase: v.price, subtotal: v.price * quantity
    };
  });
  const order: OrderRec = {
    id: orderId, customerId: customer.id, customerName: customer.name, shippingAddress: address,
    dateOrdered: dateTimeStr(new Date()), totalAmount: orderItems.reduce((sum, i) => sum + i.subtotal, 0),
    statusName: 'Pending', paymentStatus: 'Unpaid', items: orderItems
  };
  state.orders.push(order);
  return order;
}

const TRANSITIONS: Record<string, string[]> = {
  Pending: ['Paid', 'Cancelled'], Paid: ['Shipped', 'Cancelled'], Shipped: ['Delivered'], Delivered: [], Cancelled: []
};

function setOrderStatus(orderId: number, status: string): OrderRec {
  if (status === 'Cancelled') {
    return cancelOrder(orderId);
  }
  const order = findOrder(orderId);
  if (!(TRANSITIONS[order.statusName] ?? []).includes(status)) {
    throw new ApiError(400, `An order cannot move from ${order.statusName} to ${status}`);
  }
  order.statusName = status;
  return order;
}

function cancelOrder(orderId: number): OrderRec {
  const order = findOrder(orderId);
  if (order.statusName !== 'Pending' && order.statusName !== 'Paid') {
    throw new ApiError(400, 'Only Pending or Paid orders can be cancelled');
  }
  const wasPaid = order.paymentStatus === 'Paid';
  for (const item of order.items) {
    const v = findVariant(item.variantId);
    if (wasPaid) {
      v.stockQuantity += item.quantity;
    } else {
      v.reservedQuantity = Math.max(0, v.reservedQuantity - item.quantity);
    }
  }
  for (const p of state.payments.filter(x => x.orderId === orderId)) {
    if (p.status === 'Completed') {
      p.status = 'Refunded';
    } else if (p.status === 'Pending') {
      p.status = 'Failed';
    }
  }
  if (wasPaid) {
    order.paymentStatus = 'Refunded';
  }
  order.statusName = 'Cancelled';
  return order;
}

const INSTANT_METHODS = ['GCash', 'Card'];

function payOrder(orderId: number, method: string): PaymentRec {
  if (!['GCash', 'Card', 'Bank Transfer', 'COD'].includes(method)) {
    throw new ApiError(400, 'Payment method must be one of GCash, Card, Bank Transfer, COD');
  }
  const order = findOrder(orderId);
  if (order.statusName !== 'Pending' || order.paymentStatus !== 'Unpaid') {
    throw new ApiError(400, 'Only Pending, unpaid orders can be paid');
  }
  if (state.payments.some(p => p.orderId === orderId && p.status === 'Pending')) {
    throw new ApiError(400, 'This order already has a payment awaiting confirmation');
  }
  const payment: PaymentRec = {
    id: ++state.seq.payment, orderId, method, amount: order.totalAmount, paymentDate: dateTimeStr(new Date()), status: 'Pending'
  };
  state.payments.push(payment);
  return INSTANT_METHODS.includes(method) ? confirmPayment(payment.id) : payment;
}

function confirmPayment(paymentId: number): PaymentRec {
  const payment = findPayment(paymentId);
  if (payment.status !== 'Pending') {
    throw new ApiError(400, 'Only a Pending payment can be confirmed');
  }
  const order = findOrder(payment.orderId);
  if (order.statusName !== 'Pending') {
    throw new ApiError(400, `The order is ${order.statusName} and can no longer be paid`);
  }
  for (const item of order.items) {
    const v = findVariant(item.variantId);
    v.stockQuantity -= item.quantity;
    v.reservedQuantity -= item.quantity;
  }
  payment.status = 'Completed';
  payment.paymentDate = dateTimeStr(new Date());
  order.paymentStatus = 'Paid';
  order.statusName = 'Paid';
  return payment;
}

function refundPayment(paymentId: number): PaymentRec {
  const payment = findPayment(paymentId);
  if (payment.status !== 'Completed') {
    throw new ApiError(400, 'Only a Completed payment can be refunded');
  }
  const order = findOrder(payment.orderId);
  if (order.statusName !== 'Delivered') {
    throw new ApiError(400, 'Cancel the order to refund it before it ships');
  }
  payment.status = 'Refunded';
  order.paymentStatus = 'Refunded';
  return payment;
}

// ---------- reports ----------
function stockLine(v: VariantRec) {
  const p = state.products.find(x => x.id === v.productId);
  return {
    variantId: v.id, productName: p?.name ?? '', brand: p?.brand ?? '', category: p?.category ?? '', size: v.size, sku: v.sku,
    batchNumber: v.batchNumber, expiryDate: v.expiryDate, stockQuantity: v.stockQuantity, reservedQuantity: v.reservedQuantity,
    availableQuantity: available(v), status: variantStatus(v), costValue: (v.costPrice ?? 0) * v.stockQuantity
  };
}

const countsAsSale = (o: OrderRec) => o.statusName !== 'Cancelled' && o.paymentStatus === 'Paid';

// ---------- routing ----------
export interface DemoRequest {
  method: string;
  path: string; // e.g. /api/product/3
  query: (name: string) => string | null;
  body: any;
  token: string | null;
}

export function handleDemoRequest(req: DemoRequest): DemoResult {
  try {
    if (!ready) {
      state = load();
      ready = true;
    }
    return route(req);
  } catch (e) {
    if (e instanceof ApiError) {
      return { status: e.status, body: { message: e.message } };
    }
    throw e;
  }
}

const ok = (body: unknown): DemoResult => ({ status: 200, body });
const noContent = (): DemoResult => ({ status: 204, body: null });

function customerView(c: CustomerRec) {
  const { password, ...rest } = c;
  return rest;
}

function route(req: DemoRequest): DemoResult {
  const { method, path, body } = req;
  const seg = path.replace(/^\/api\//, '').split('/');
  const id = Number(seg[1]);
  const who = req.token?.startsWith('demo-') ? state.customers.find(c => c.id === Number(req.token!.slice(5))) : undefined;
  const requireUser = (): CustomerRec => {
    if (!who) {
      throw new ApiError(401, 'Please sign in');
    }
    return who;
  };
  const requireOwner = (): CustomerRec => {
    const u = requireUser();
    if (u.role !== 'OWNER') {
      throw new ApiError(403, 'Only the owner can do this');
    }
    return u;
  };
  const isOwner = who?.role === 'OWNER';
  const mine = (o: OrderRec) => isOwner || o.customerId === who?.id;
  const orderView = (o: OrderRec) => ({ ...o, items: o.items.map(i => ({ ...i })) });

  // ----- customers -----
  if (seg[0] === 'customer') {
    if (method === 'POST' && seg[1] === 'login') {
      const c = state.customers.find(x => x.email.toLowerCase() === String(body?.email ?? '').trim().toLowerCase());
      if (!c || c.password !== body?.password) {
        throw new ApiError(401, 'Wrong email or password');
      }
      return ok({ token: `demo-${c.id}`, customer: customerView(c) });
    }
    if (method === 'POST' && seg[1] === 'register') {
      const email = String(body?.email ?? '').trim().toLowerCase();
      if (!body?.name?.trim() || !email || !body?.password) {
        throw new ApiError(400, 'Name, email and password are required');
      }
      if (String(body.password).length < 6) {
        throw new ApiError(400, 'Password must be at least 6 characters');
      }
      if (state.customers.some(c => c.email.toLowerCase() === email)) {
        throw new ApiError(400, 'That email is already registered');
      }
      const c: CustomerRec = {
        id: ++state.seq.customer, name: body.name.trim(), email, role: 'CUSTOMER', password: body.password,
        phone: body.phone || undefined, shippingAddress: body.shippingAddress || undefined
      };
      state.customers.push(c);
      save();
      return ok({ token: `demo-${c.id}`, customer: customerView(c) });
    }
    if (method === 'PUT' && seg.length === 1) {
      const u = requireUser();
      u.name = body?.name?.trim() || u.name;
      u.phone = body?.phone ?? u.phone;
      u.shippingAddress = body?.shippingAddress ?? u.shippingAddress;
      save();
      return ok(customerView(u));
    }
    if (method === 'POST' && seg[1] === 'forgot-password') {
      return ok({ message: 'If that email is registered, a reset link has been sent. (Demo: no email is actually sent.)' });
    }
    if (method === 'POST' && seg[1] === 'reset-password') {
      throw new ApiError(400, 'Password reset is not available in the demo.');
    }
    if (method === 'POST' && seg[1] === 'logout') {
      return noContent();
    }
  }

  // ----- products -----
  if (seg[0] === 'product') {
    if (method === 'GET' && seg.length === 1) {
      const kw = (req.query('keyword') ?? '').toLowerCase();
      const cat = req.query('category') ?? '';
      const brand = (req.query('brand') ?? '').toLowerCase();
      return ok(state.products
        .filter(p => (!kw || `${p.name} ${p.brand} ${p.description}`.toLowerCase().includes(kw)) && (!cat || p.category === cat) && (!brand || p.brand.toLowerCase() === brand))
        .map(p => productView(p, isOwner)));
    }
    if (method === 'GET') {
      const p = state.products.find(x => x.id === id);
      if (!p) {
        throw new ApiError(404, `Product ${id} not found`);
      }
      return ok(productView(p, isOwner));
    }
    requireOwner();
    if (method === 'POST' || method === 'PUT') {
      if (!body?.name?.trim() || !body?.brand?.trim() || !body?.category) {
        throw new ApiError(400, 'Name, brand and category are required');
      }
      let p = method === 'PUT' ? state.products.find(x => x.id === body.id) : undefined;
      if (method === 'PUT' && !p) {
        throw new ApiError(404, `Product ${body.id} not found`);
      }
      if (!p) {
        p = { id: ++state.seq.product, name: '', brand: '', description: '', category: '', imageUrl: null, dateAdded: today() };
        state.products.push(p);
      }
      Object.assign(p, {
        name: body.name.trim(), brand: body.brand.trim(), category: body.category,
        description: body.description ?? '', imageUrl: body.imageUrl || null
      });
      save();
      return ok(productView(p, true));
    }
    if (method === 'DELETE') {
      if (state.orders.some(o => o.items.some(i => state.variants.find(v => v.id === i.variantId)?.productId === id))) {
        throw new ApiError(400, 'This perfume has orders and cannot be deleted. Discontinue its sizes instead.');
      }
      state.variants = state.variants.filter(v => v.productId !== id);
      state.products = state.products.filter(p => p.id !== id);
      save();
      return noContent();
    }
  }

  // ----- variants -----
  if (seg[0] === 'variant') {
    requireOwner();
    if (method === 'GET' && seg[1] === 'expiring') {
      const limit = dateStr(addDays(new Date(), Number(req.query('days') ?? 90)));
      return ok(state.variants.filter(v => !v.discontinued && v.expiryDate && v.expiryDate <= limit).map(v => variantView(v, true)));
    }
    if (method === 'GET' && seg[1] === 'low-stock') {
      const threshold = Number(req.query('threshold') ?? LOW_STOCK_THRESHOLD);
      return ok(state.variants.filter(v => !v.discontinued && available(v) <= threshold).map(v => variantView(v, true)));
    }
    if ((method === 'POST' || method === 'PUT') && seg.length === 1) {
      if (!body?.size?.trim() || !(body?.price >= 0) || !(body?.stockQuantity >= 0)) {
        throw new ApiError(400, 'Size, price and stock are required');
      }
      if (!state.products.some(p => p.id === body.productId)) {
        throw new ApiError(404, `Product ${body.productId} not found`);
      }
      let v = body.id ? findVariant(body.id) : undefined;
      if (!v) {
        v = {
          id: ++state.seq.variant, productId: body.productId, size: '', sku: '', price: 0, costPrice: null, stockQuantity: 0,
          reservedQuantity: 0, batchNumber: '', manufactureDate: null, expiryDate: null, discontinued: false
        };
        state.variants.push(v);
      }
      Object.assign(v, {
        size: body.size.trim(), sku: body.sku || v.sku || `AM-${v.productId}-${body.size.trim().toUpperCase()}`,
        price: Number(body.price), costPrice: body.costPrice === null || body.costPrice === '' ? null : Number(body.costPrice),
        stockQuantity: Number(body.stockQuantity), batchNumber: body.batchNumber ?? '',
        manufactureDate: body.manufactureDate || null, expiryDate: body.expiryDate || null
      });
      save();
      return ok(variantView(v, true));
    }
    const v = findVariant(id);
    if (method === 'PUT' && seg[2] === 'stock') {
      const q = Number(body?.quantity);
      if (!(q >= v.reservedQuantity)) {
        throw new ApiError(400, `Stock cannot be lower than the ${v.reservedQuantity} already reserved`);
      }
      v.stockQuantity = q;
      save();
      return ok(variantView(v, true));
    }
    if (method === 'PUT' && seg[2] === 'deactivate') {
      v.discontinued = true;
      save();
      return ok(variantView(v, true));
    }
    if (method === 'DELETE') {
      if (state.orders.some(o => o.items.some(i => i.variantId === id))) {
        throw new ApiError(400, 'This size has orders and cannot be deleted. Discontinue it instead.');
      }
      state.variants = state.variants.filter(x => x.id !== id);
      save();
      return noContent();
    }
  }

  // ----- orders -----
  if (seg[0] === 'order') {
    if (method === 'GET' && seg.length === 1) {
      requireOwner();
      return ok([...state.orders].reverse().map(orderView));
    }
    if (method === 'GET' && seg[1] === 'customer') {
      const u = requireUser();
      const customerId = Number(seg[2]);
      if (!isOwner && u.id !== customerId) {
        throw new ApiError(403, 'You can only see your own orders');
      }
      return ok(state.orders.filter(o => o.customerId === customerId).reverse().map(orderView));
    }
    if (method === 'GET') {
      requireUser();
      const o = findOrder(id);
      if (!mine(o)) {
        throw new ApiError(403, 'You can only see your own orders');
      }
      return ok(orderView(o));
    }
    if (method === 'POST' && seg.length === 1) {
      const u = requireUser();
      const o = placeOrder(u, body?.items, body?.shippingAddress);
      save();
      return ok(orderView(o));
    }
    if (method === 'PUT' && seg[2] === 'status') {
      requireOwner();
      const o = setOrderStatus(id, String(body?.status));
      save();
      return ok(orderView(o));
    }
    if (method === 'POST' && seg[2] === 'cancel') {
      requireUser();
      if (!mine(findOrder(id))) {
        throw new ApiError(403, 'You can only cancel your own orders');
      }
      const o = cancelOrder(id);
      save();
      return ok(orderView(o));
    }
  }

  // ----- payments -----
  if (seg[0] === 'payment') {
    if (method === 'POST' && seg.length === 1) {
      requireUser();
      if (!mine(findOrder(Number(body?.orderId)))) {
        throw new ApiError(403, 'You can only pay for your own orders');
      }
      const p = payOrder(Number(body.orderId), body.method);
      save();
      return ok(p);
    }
    if (method === 'GET' && seg[1] === 'order') {
      requireUser();
      const orderId = Number(seg[2]);
      if (!mine(findOrder(orderId))) {
        throw new ApiError(403, 'You can only see your own payments');
      }
      return ok(state.payments.filter(p => p.orderId === orderId));
    }
    if (method === 'PUT' && seg[2] === 'confirm') {
      requireOwner();
      const p = confirmPayment(id);
      save();
      return ok(p);
    }
    if (method === 'POST' && seg[2] === 'refund') {
      requireOwner();
      const p = refundPayment(id);
      save();
      return ok(p);
    }
  }

  // ----- reports (owner) -----
  if (seg[0] === 'report') {
    requireOwner();
    if (seg[1] === 'stock') {
      return ok(state.variants.map(stockLine).sort((a, b) => a.productName.localeCompare(b.productName)));
    }
    if (seg[1] === 'best-sellers') {
      const end = req.query('to') ?? today();
      const endDate = new Date(end + 'T00:00:00');
      const start = req.query('from') ?? dateStr(addMonths(endDate, -1));
      const sales = new Map<number, { variantId: number; productName: string; size: string; unitsSold: number; revenue: number }>();
      for (const o of state.orders.filter(countsAsSale)) {
        const day = o.dateOrdered.slice(0, 10);
        if (day < start || day > end) {
          continue;
        }
        for (const i of o.items) {
          const row = sales.get(i.variantId) ?? { variantId: i.variantId, productName: i.productName, size: i.size, unitsSold: 0, revenue: 0 };
          row.unitsSold += i.quantity;
          row.revenue += i.subtotal;
          sales.set(i.variantId, row);
        }
      }
      return ok([...sales.values()].sort((a, b) => b.unitsSold - a.unitsSold));
    }
    if (seg[1] === 'slow-moving') {
      const since = dateTimeStr(addMonths(new Date(), -Number(req.query('months') ?? 3)));
      const sold = new Set(state.orders.filter(o => o.statusName !== 'Cancelled' && o.dateOrdered >= since).flatMap(o => o.items.map(i => i.variantId)));
      return ok(state.variants.filter(v => v.stockQuantity > 0 && !sold.has(v.id) && !v.discontinued).map(stockLine).sort((a, b) => b.costValue - a.costValue));
    }
    if (seg[1] === 'revenue-by-category') {
      const totals = new Map<string, number>();
      for (const o of state.orders.filter(countsAsSale)) {
        for (const i of o.items) {
          const category = state.products.find(p => p.id === state.variants.find(v => v.id === i.variantId)?.productId)?.category ?? 'Other';
          totals.set(category, (totals.get(category) ?? 0) + i.subtotal);
        }
      }
      return ok([...totals].map(([category, revenue]) => ({ category, revenue })).sort((a, b) => b.revenue - a.revenue));
    }
    if (seg[1] === 'valuation') {
      return ok({
        totalUnits: state.variants.reduce((n, v) => n + v.stockQuantity, 0),
        costValue: state.variants.reduce((n, v) => n + (v.costPrice ?? 0) * v.stockQuantity, 0),
        retailValue: state.variants.reduce((n, v) => n + v.price * v.stockQuantity, 0)
      });
    }
  }

  throw new ApiError(404, `No demo endpoint for ${method} ${path}`);
}

/** Wipes the visitor's demo data and starts over. */
export function resetDemo(): void {
  try {
    localStorage.removeItem(STORAGE_KEY);
  } catch {
    /* nothing to clear */
  }
  state = seed();
  ready = true;
}
