import { startTransition, useDeferredValue, useEffect, useMemo, useState } from "react";
import { api } from "../lib/api";
import { useAppContext } from "../context/AppContext";

function currencyLabel(value, currency) {
  const amount = Number(value ?? 0);
  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: currency || "USD"
  }).format(amount);
}

function formatRole(role) {
  return (role ?? "")
    .replace(/^ROLE_/, "")
    .toLowerCase()
    .replace(/\b\w/g, (char) => char.toUpperCase());
}

function ProductCard({ product, inventory, onAdd }) {
  return (
    <article className="card product-card">
      <div className="product-head">
        <div>
          <span className="pill">{product.category || "General"}</span>
          <h3>{product.name}</h3>
        </div>
        <strong>{currencyLabel(product.price, product.currency)}</strong>
      </div>
      <p>{product.description}</p>
      <div className="card-meta">
        <span>Brand: {product.brand || "Unspecified"}</span>
        <span>Available: {inventory?.availableQuantity ?? "..."}</span>
      </div>
      <button onClick={() => onAdd(product)}>Add to cart</button>
    </article>
  );
}

function CartPanel({ cart, onUpdate, onCheckout, checkoutBusy, checkoutStatus }) {
  const total = cart.reduce((sum, item) => sum + item.price * item.quantity, 0);
  const currency = cart[0]?.currency || "USD";

  return (
    <section className="panel">
      <div className="panel-title">
        <h2>Cart</h2>
        <span>{cart.length} items</span>
      </div>

      <div className="list-stack">
        {cart.length === 0 ? (
          <p className="muted">Add products to start checkout.</p>
        ) : (
          cart.map((item) => (
            <div className="list-row" key={item.id}>
              <div>
                <strong>{item.name}</strong>
                <p>{currencyLabel(item.price, item.currency)} each</p>
              </div>
              <div className="quantity-stepper">
                <button onClick={() => onUpdate(item.id, item.quantity - 1)}>-</button>
                <span>{item.quantity}</span>
                <button onClick={() => onUpdate(item.id, item.quantity + 1)}>+</button>
              </div>
            </div>
          ))
        )}
      </div>

      <div className="summary-box">
        <span>Total</span>
        <strong>{currencyLabel(total, currency)}</strong>
      </div>
      <button disabled={cart.length === 0 || checkoutBusy} onClick={onCheckout}>
        {checkoutBusy ? "Placing order..." : "Checkout"}
      </button>
      {checkoutStatus ? <p className="success-text">{checkoutStatus}</p> : null}
    </section>
  );
}

function OrdersPanel({ orders, workflows, payments, notifications, onRefresh, onCancel }) {
  return (
    <section className="panel">
      <div className="panel-title">
        <h2>Orders and notifications</h2>
        <button className="ghost" onClick={onRefresh}>
          Refresh
        </button>
      </div>

      <div className="list-stack">
        {orders.length === 0 ? (
          <p className="muted">No orders placed yet.</p>
        ) : (
          orders.map((order) => {
            const workflow = workflows[order.id];
            const payment = payments[order.id];
            const orderNotifications = notifications.filter((item) => item.orderId === order.id);

            return (
              <article className="order-card" key={order.id}>
                <div className="order-head">
                  <div>
                    <strong>{order.id}</strong>
                    <p>{order.items.length} items</p>
                  </div>
                  <div className="order-status">
                    <span className="pill">{order.status}</span>
                    <span className="pill alt">{order.paymentStatus}</span>
                  </div>
                </div>

                <div className="order-grid">
                  <div>
                    <dt>Workflow</dt>
                    <dd>{workflow?.lastEventType ?? "-"}</dd>
                  </div>
                  <div>
                    <dt>Correlation ID</dt>
                    <dd>{workflow?.correlationId ?? "-"}</dd>
                  </div>
                  <div>
                    <dt>Payment</dt>
                    <dd>{payment?.status ?? "-"}</dd>
                  </div>
                  <div>
                    <dt>Trace ID</dt>
                    <dd>{workflow?.traceId ?? "-"}</dd>
                  </div>
                </div>

                <div className="mini-list">
                  {order.items.map((item) => (
                    <span key={item.id || `${order.id}-${item.productId}`}>
                      {item.productId} x {item.quantity}
                    </span>
                  ))}
                </div>

                <div className="card-actions">
                  <button className="ghost" onClick={() => onRefresh()}>
                    Refresh status
                  </button>
                  {order.status !== "CANCELLED" && order.status !== "FAILED" ? (
                    <button className="danger" onClick={() => onCancel(order.id)}>
                      Cancel order
                    </button>
                  ) : null}
                </div>

                {orderNotifications.length > 0 ? (
                  <div className="notification-strip">
                    {orderNotifications.map((notification) => (
                      <span key={notification.id}>
                        {notification.type}: {notification.message}
                      </span>
                    ))}
                  </div>
                ) : null}
              </article>
            );
          })
        )}
      </div>
    </section>
  );
}

function AdminPanel({ token, onCreated }) {
  const [productForm, setProductForm] = useState({
    name: "",
    description: "",
    price: "",
    currency: "USD",
    category: "",
    brand: ""
  });
  const [inventoryForm, setInventoryForm] = useState({
    productId: "",
    quantity: "",
    warehouseLocation: ""
  });
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  async function handleCreateProduct(event) {
    event.preventDefault();
    setMessage("");
    setError("");

    try {
      await api.createProduct(
        {
          ...productForm,
          price: Number(productForm.price)
        },
        token
      );
      setMessage("Product created successfully.");
      setProductForm({
        name: "",
        description: "",
        price: "",
        currency: "USD",
        category: "",
        brand: ""
      });
      onCreated();
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  async function handleAddInventory(event) {
    event.preventDefault();
    setMessage("");
    setError("");

    try {
      await api.addInventory(
        {
          ...inventoryForm,
          quantity: Number(inventoryForm.quantity)
        },
        token
      );
      setMessage("Inventory added successfully.");
      setInventoryForm({
        productId: "",
        quantity: "",
        warehouseLocation: ""
      });
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  return (
    <section className="panel">
      <div className="panel-title">
        <h2>Admin operations</h2>
        <span>Uses current backend endpoints only</span>
      </div>

      <div className="admin-grid">
        <form className="stacked-form compact" onSubmit={handleCreateProduct}>
          <h3>Create product</h3>
          <input
            placeholder="Name"
            value={productForm.name}
            onChange={(event) => setProductForm({ ...productForm, name: event.target.value })}
            required
          />
          <textarea
            placeholder="Description"
            value={productForm.description}
            onChange={(event) => setProductForm({ ...productForm, description: event.target.value })}
            required
          />
          <input
            placeholder="Price"
            type="number"
            min="0"
            step="0.01"
            value={productForm.price}
            onChange={(event) => setProductForm({ ...productForm, price: event.target.value })}
            required
          />
          <input
            placeholder="Currency"
            value={productForm.currency}
            onChange={(event) => setProductForm({ ...productForm, currency: event.target.value })}
            required
          />
          <input
            placeholder="Category"
            value={productForm.category}
            onChange={(event) => setProductForm({ ...productForm, category: event.target.value })}
          />
          <input
            placeholder="Brand"
            value={productForm.brand}
            onChange={(event) => setProductForm({ ...productForm, brand: event.target.value })}
          />
          <button type="submit">Save product</button>
        </form>

        <form className="stacked-form compact" onSubmit={handleAddInventory}>
          <h3>Add inventory</h3>
          <input
            placeholder="Product ID"
            value={inventoryForm.productId}
            onChange={(event) => setInventoryForm({ ...inventoryForm, productId: event.target.value })}
            required
          />
          <input
            placeholder="Quantity"
            type="number"
            min="1"
            value={inventoryForm.quantity}
            onChange={(event) => setInventoryForm({ ...inventoryForm, quantity: event.target.value })}
            required
          />
          <input
            placeholder="Warehouse location"
            value={inventoryForm.warehouseLocation}
            onChange={(event) =>
              setInventoryForm({ ...inventoryForm, warehouseLocation: event.target.value })
            }
          />
          <button type="submit">Add stock</button>
        </form>
      </div>

      {message ? <p className="success-text">{message}</p> : null}
      {error ? <p className="error-text">{error}</p> : null}
    </section>
  );
}

export function ShopPage() {
  const { session, cart, addToCart, updateCartQuantity, clearCart, logout, isAdmin } = useAppContext();
  const [products, setProducts] = useState([]);
  const [inventoryByProductId, setInventoryByProductId] = useState({});
  const [orders, setOrders] = useState([]);
  const [workflows, setWorkflows] = useState({});
  const [payments, setPayments] = useState({});
  const [notifications, setNotifications] = useState([]);
  const [search, setSearch] = useState("");
  const [status, setStatus] = useState("");
  const [error, setError] = useState("");
  const [checkoutBusy, setCheckoutBusy] = useState(false);

  const deferredSearch = useDeferredValue(search);
  const displayName = session.displayName?.trim() || session.userId;
  const displayRole = formatRole(session.role) || "User";

  const visibleProducts = useMemo(() => {
    const query = deferredSearch.trim().toLowerCase();
    if (!query) {
      return products;
    }

    return products.filter((product) =>
      [product.name, product.description, product.category, product.brand]
        .filter(Boolean)
        .some((value) => value.toLowerCase().includes(query))
    );
  }, [deferredSearch, products]);

  async function loadProducts() {
    try {
      const response = await api.getProducts(session.accessToken);
      const nextProducts = response.data ?? [];
      setProducts(nextProducts);

      const inventories = await Promise.allSettled(
        nextProducts.map(async (product) => {
          const inventoryResponse = await api.getInventory(product.id, session.accessToken);
          return [product.id, inventoryResponse.data];
        })
      );

      const nextInventory = {};
      inventories.forEach((result) => {
        if (result.status === "fulfilled") {
          const [productId, inventory] = result.value;
          nextInventory[productId] = inventory;
        }
      });
      setInventoryByProductId(nextInventory);
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  async function loadOrders() {
    try {
      const orderList = await api.getOrderHistory(session.userId, session.accessToken);
      setOrders(orderList);

      const [workflowResults, paymentResults] = await Promise.all([
        Promise.allSettled(orderList.map((order) => api.getOrderWorkflow(order.id, session.accessToken))),
        Promise.allSettled(orderList.map((order) => api.getPaymentForOrder(order.id, session.accessToken)))
      ]);

      const nextWorkflows = {};
      workflowResults.forEach((result, index) => {
        if (result.status === "fulfilled") {
          nextWorkflows[orderList[index].id] = result.value;
        }
      });

      const nextPayments = {};
      paymentResults.forEach((result, index) => {
        if (result.status === "fulfilled") {
          nextPayments[orderList[index].id] = result.value;
        }
      });

      setWorkflows(nextWorkflows);
      setPayments(nextPayments);
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  async function loadNotifications() {
    try {
      const nextNotifications = await api.getNotifications(session.userId, session.accessToken);
      setNotifications(nextNotifications);
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  useEffect(() => {
    loadProducts();
    loadOrders();
    loadNotifications();
  }, [session.accessToken, session.userId]);

  async function handleCheckout() {
    setCheckoutBusy(true);
    setError("");
    setStatus("");

    try {
      const createdOrder = await api.createOrder(
        {
          userId: session.userId,
          currency: cart[0]?.currency || "USD",
          items: cart.map((item) => ({
            productId: item.id,
            quantity: item.quantity,
            price: item.price
          }))
        },
        session.accessToken
      );

      clearCart();
      setStatus(`Order ${createdOrder.id} placed successfully.`);
      await Promise.all([loadOrders(), loadNotifications(), loadProducts()]);
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setCheckoutBusy(false);
    }
  }

  async function handleCancel(orderId) {
    setError("");
    setStatus("");
    try {
      await api.cancelOrder(orderId, session.accessToken);
      setStatus(`Order ${orderId} cancelled.`);
      await Promise.all([loadOrders(), loadNotifications(), loadProducts()]);
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  return (
    <div className="app-shell">
      <header className="hero-banner">
        <div>
          <p className="eyebrow">SmartCart workspace</p>
          <h1>Storefront, checkout, order tracking, and admin stock control</h1>
        </div>
        <div className="hero-actions">
          <span className="session-chip">
            {displayName} | {displayRole}
          </span>
          <button className="ghost" onClick={logout}>
            Logout
          </button>
        </div>
      </header>

      {status ? <p className="success-text banner-text">{status}</p> : null}
      {error ? <p className="error-text banner-text">{error}</p> : null}

      <section className="panel">
        <div className="panel-title">
          <h2>Catalog</h2>
          <input
            className="search-input"
            placeholder="Search products"
            value={search}
            onChange={(event) => {
              const value = event.target.value;
              startTransition(() => setSearch(value));
            }}
          />
        </div>
        <div className="catalog-grid">
          {visibleProducts.map((product) => (
            <ProductCard
              key={product.id}
              product={product}
              inventory={inventoryByProductId[product.id]}
              onAdd={addToCart}
            />
          ))}
        </div>
      </section>

      <div className="dashboard-grid">
        <CartPanel
          cart={cart}
          onUpdate={updateCartQuantity}
          onCheckout={handleCheckout}
          checkoutBusy={checkoutBusy}
          checkoutStatus={status.includes("placed successfully") ? status : ""}
        />
        <OrdersPanel
          orders={orders}
          workflows={workflows}
          payments={payments}
          notifications={notifications}
          onRefresh={() => Promise.all([loadOrders(), loadNotifications(), loadProducts()])}
          onCancel={handleCancel}
        />
      </div>

      {isAdmin ? <AdminPanel token={session.accessToken} onCreated={loadProducts} /> : null}
    </div>
  );
}
