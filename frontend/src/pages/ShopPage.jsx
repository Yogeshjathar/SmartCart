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

function productCode(product) {
  return product?.sku || product?.id || "";
}

function stockMeta(inventory) {
  const available = inventory?.availableQuantity ?? 0;

  if (available <= 0) {
    return {
      label: "Out of stock",
      tone: "critical",
      detail: "Restock required before checkout"
    };
  }

  if (available <= 5) {
    return {
      label: "Few units left",
      tone: "warning",
      detail: `${available} ready to ship`
    };
  }

  return {
    label: "In stock",
    tone: "healthy",
    detail: `${available} ready to ship`
  };
}

function ratingFor(product) {
  const source = `${product.name}${product.brand}${product.category}`;
  const total = source.split("").reduce((sum, char) => sum + char.charCodeAt(0), 0);
  return (4 + (total % 9) / 10).toFixed(1);
}

function formatNotificationType(value) {
  return (value ?? "")
    .toLowerCase()
    .split("_")
    .filter(Boolean)
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(" ");
}

function cartIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <path
        d="M3 4h2l2.2 10.2a1 1 0 0 0 1 .8h8.9a1 1 0 0 0 1-.8L20 7H7"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <circle cx="10" cy="19" r="1.6" fill="currentColor" />
      <circle cx="17" cy="19" r="1.6" fill="currentColor" />
    </svg>
  );
}

function bellIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <path
        d="M6 16h12l-1.4-1.7a3.2 3.2 0 0 1-.8-2.1V10a3.8 3.8 0 1 0-7.6 0v2.2a3.2 3.2 0 0 1-.8 2.1L6 16Z"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <path
        d="M10 18a2.2 2.2 0 0 0 4 0"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
      />
    </svg>
  );
}

function orderItemsSummary(items, productsById) {
  const names = items.map((item) => productsById[item.productId]?.name || "Product").filter(Boolean);
  if (names.length === 0) {
    return "Order items";
  }
  if (names.length === 1) {
    return names[0];
  }
  if (names.length === 2) {
    return `${names[0]} and ${names[1]}`;
  }
  return `${names[0]}, ${names[1]} +${names.length - 2} more`;
}

function formatOrderDate(value) {
  if (!value) {
    return "Recently placed";
  }

  return new Intl.DateTimeFormat("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric"
  }).format(new Date(value));
}

function shortOrderId(value) {
  if (!value) {
    return "-";
  }

  const compact = String(value).replace(/-/g, "").toUpperCase();
  return `#${compact.slice(0, 8)}`;
}

function compareOrdersNewestFirst(left, right) {
  const leftTime = left?.createdAt ? new Date(left.createdAt).getTime() : 0;
  const rightTime = right?.createdAt ? new Date(right.createdAt).getTime() : 0;

  if (leftTime !== rightTime) {
    return rightTime - leftTime;
  }

  return String(right?.id || "").localeCompare(String(left?.id || ""));
}

function orderTimeline(order, workflow, payment) {
  const paymentState = payment?.status || order.paymentStatus || "NOT_STARTED";
  const orderState = order.status || "CREATED";

  const steps = [
    { key: "placed", label: "Order Placed", icon: "1", active: true },
    {
      key: "payment",
      label: "Payment Confirmed",
      icon: "2",
      active: paymentState === "SUCCESS" || paymentState === "PAID" || orderState === "CONFIRMED"
    },
    {
      key: "processing",
      label: "Processing",
      icon: "3",
      active: ["RESERVED", "CONFIRMED"].includes(orderState)
    },
    {
      key: "shipped",
      label: "Shipped",
      icon: "4",
      active: orderState === "SHIPPED" || workflow?.lastEventType === "ORDER_SHIPPED"
    },
    {
      key: "delivered",
      label: "Delivered",
      icon: "5",
      active: orderState === "DELIVERED" || workflow?.lastEventType === "ORDER_DELIVERED"
    }
  ];

  let currentKey = "placed";
  if (steps[4].active) {
    currentKey = "delivered";
  } else if (steps[3].active) {
    currentKey = "shipped";
  } else if (steps[2].active) {
    currentKey = "processing";
  } else if (steps[1].active) {
    currentKey = "payment";
  }

  return steps.map((step) => ({
    ...step,
    current: step.key === currentKey
  }));
}

function MarketplaceHero({
  displayName,
  displayRole,
  totalProducts,
  categoriesCount,
  inStockCount,
  search,
  onSearchChange,
  unreadNotificationCount,
  cartCount,
  recentOrders,
  onOpenCart,
  onOpenNotifications,
  onOpenOrders,
  profileOpen,
  onToggleProfile,
  logout
}) {
  return (
    <header className="market-hero">
      <div className="market-hero-copy">
        <p className="eyebrow">SmartCart Marketplace</p>
        <h1>Find fast-moving products with live stock confidence.</h1>
        <p className="hero-copy-text">
          Search across catalog, compare stock readiness, and check out from one customer-first
          workspace built for real inventory visibility.
        </p>
        <div className="hero-metrics">
          <div className="hero-metric">
            <strong>{totalProducts}</strong>
            <span>Live listings</span>
          </div>
          <div className="hero-metric">
            <strong>{categoriesCount}</strong>
            <span>Categories</span>
          </div>
          <div className="hero-metric">
            <strong>{inStockCount}</strong>
            <span>Ready to ship</span>
          </div>
        </div>
        <div className="hero-search-shell">
          <input
            className="hero-search-input"
            placeholder="Search by SKU, product, brand, or category"
            value={search}
            onChange={(event) => {
              const value = event.target.value;
              startTransition(() => onSearchChange(value));
            }}
          />
        </div>
      </div>

      <div className="market-hero-side">
        <div className="identity-card">
          <button type="button" className="header-icon-button" onClick={onOpenCart}>
            <span className="header-icon-glyph">{cartIcon()}</span>
            <span>Cart</span>
            {cartCount > 0 ? <strong>{cartCount}</strong> : null}
          </button>

          <button type="button" className="header-icon-button" onClick={onOpenNotifications}>
            <span className="header-icon-glyph">{bellIcon()}</span>
            <span>Alerts</span>
            {unreadNotificationCount > 0 ? (
              <strong>{unreadNotificationCount > 9 ? "9+" : unreadNotificationCount}</strong>
            ) : null}
          </button>

          <div className="profile-menu-shell">
            <button type="button" className="profile-trigger" onClick={onToggleProfile}>
              <span className="profile-avatar">{displayName.charAt(0).toUpperCase()}</span>
              <span className="profile-trigger-copy">
                <strong>{displayName}</strong>
                <span>{displayRole}</span>
              </span>
            </button>

            {profileOpen ? (
              <div className="profile-dropdown">
                <button type="button" className="profile-dropdown-item" onClick={onOpenCart}>
                  <strong>My Cart</strong>
                  <span>{cartCount} products ready for checkout</span>
                </button>
                <button type="button" className="profile-dropdown-item" onClick={onOpenOrders}>
                  <strong>My Orders</strong>
                  <span>{recentOrders.length} recent orders</span>
                </button>
                <button
                  type="button"
                  className="profile-dropdown-item"
                  onClick={onOpenNotifications}
                >
                  <strong>Notifications</strong>
                  <span>{unreadNotificationCount} unread updates</span>
                </button>
                <button type="button" className="profile-dropdown-item" onClick={logout}>
                  <strong>Logout</strong>
                  <span>End current session</span>
                </button>
              </div>
            ) : null}
          </div>
        </div>

        <div className="trust-panel">
          <div>
            <p className="trust-title">Why this storefront feels reliable</p>
            <p className="trust-copy">
              Product cards surface current inventory, shipping readiness, and business SKU instead
              of exposing internal IDs to customers.
            </p>
          </div>
          <div className="trust-pills">
            <span>Live stock signals</span>
            <span>SKU-first catalog</span>
            <span>Order workflow tracking</span>
          </div>
        </div>
      </div>
    </header>
  );
}

function MerchRail({ featuredProducts, inventoryByProductId, onSelectCategory }) {
  return (
    <section className="merch-rail">
      {featuredProducts.map((product, index) => {
        const inventory = inventoryByProductId[product.id];
        const meta = stockMeta(inventory);

        return (
          <article className="merch-card" key={product.id}>
            <span className="merch-rank">0{index + 1}</span>
            <strong>{product.name}</strong>
            <p>{product.brand || product.category || "General catalog pick"}</p>
            <div className="merch-card-meta">
              <span>{productCode(product)}</span>
              <span>{meta.detail}</span>
            </div>
            <button className="ghost" onClick={() => onSelectCategory(product.category || "All")}>
              Explore {product.category || "Catalog"}
            </button>
          </article>
        );
      })}
    </section>
  );
}

function CatalogToolbar({
  categories,
  activeCategory,
  onCategoryChange,
  sortBy,
  onSortChange,
  resultCount
}) {
  return (
    <section className="panel catalog-toolbar">
      <div className="panel-title">
        <div>
          <h2>Discover products</h2>
          <span>{resultCount} products match your current view</span>
        </div>
        <div className="toolbar-controls">
          <label className="toolbar-select">
            Sort
            <select value={sortBy} onChange={(event) => onSortChange(event.target.value)}>
              <option value="featured">Featured</option>
              <option value="price-asc">Price: Low to high</option>
              <option value="price-desc">Price: High to low</option>
              <option value="stock-desc">Stock: High to low</option>
              <option value="name-asc">Name: A to Z</option>
            </select>
          </label>
        </div>
      </div>

      <div className="category-row">
        {categories.map((category) => (
          <button
            key={category}
            type="button"
            className={`category-chip ${activeCategory === category ? "active" : ""}`}
            onClick={() => onCategoryChange(category)}
          >
            {category}
          </button>
        ))}
      </div>
    </section>
  );
}

function ProductCard({ product, inventory, onAdd }) {
  const meta = stockMeta(inventory);
  const available = inventory?.availableQuantity ?? 0;
  const rating = ratingFor(product);

  return (
    <article className="product-card">
      <div className="product-card-top">
        <span className={`stock-pill ${meta.tone}`}>{meta.label}</span>
        <span className="product-rating">{rating} / 5</span>
      </div>

      <div className="product-card-body">
        <div className="product-card-copy">
          <p className="product-card-category">{product.category || "General"}</p>
          <h3>{product.name}</h3>
          <p className="product-card-description">{product.description}</p>
        </div>

        <div className="product-card-priceband">
          <strong>{currencyLabel(product.price, product.currency)}</strong>
          <span>Seller: {product.brand || "SmartCart Select"}</span>
        </div>

        <div className="product-facts">
          <span>SKU {productCode(product)}</span>
          <span>{meta.detail}</span>
          <span>{available > 0 ? "Estimated dispatch in 24h" : "Dispatch resumes after restock"}</span>
        </div>
      </div>

      <div className="product-card-actions">
        <button disabled={available <= 0} onClick={() => onAdd(product)}>
          {available > 0 ? "Add to cart" : "Unavailable"}
        </button>
      </div>
    </article>
  );
}

function CartContent({ cart, onUpdate, onCheckout, checkoutBusy, checkoutStatus }) {
  const total = cart.reduce((sum, item) => sum + item.price * item.quantity, 0);
  const currency = cart[0]?.currency || "USD";
  const totalUnits = cart.reduce((sum, item) => sum + item.quantity, 0);

  return (
    <>
      <div className="list-stack">
        {cart.length === 0 ? (
          <p className="muted">Add products to start checkout.</p>
        ) : (
          cart.map((item) => (
            <div className="list-row basket-row" key={item.id}>
              <div>
                <strong>{item.name}</strong>
                <p>{productCode(item)}</p>
              </div>
              <div className="basket-side">
                <p>{currencyLabel(item.price, item.currency)} each</p>
                <div className="quantity-stepper">
                  <button onClick={() => onUpdate(item.id, item.quantity - 1)}>-</button>
                  <span>{item.quantity}</span>
                  <button onClick={() => onUpdate(item.id, item.quantity + 1)}>+</button>
                </div>
              </div>
            </div>
          ))
        )}
      </div>

      <div className="summary-box checkout-box">
        <div>
          <span>Order total</span>
          <strong>{currencyLabel(total, currency)}</strong>
        </div>
        <p>Live stock will be revalidated during checkout.</p>
      </div>
      <button disabled={cart.length === 0 || checkoutBusy} onClick={onCheckout}>
        {checkoutBusy ? "Placing order..." : "Secure checkout"}
      </button>
      {checkoutStatus ? <p className="success-text">{checkoutStatus}</p> : null}
    </>
  );
}

function ActivityDrawer({
  mode,
  open,
  cart,
  orders,
  notifications,
  readNotificationIds,
  productsById,
  workflows,
  payments,
  checkoutBusy,
  checkoutStatus,
  onUpdateCart,
  onCheckout,
  onClose,
  onRefresh,
  onCancel,
  onNotificationSeen
}) {
  const showCart = mode === "cart";
  const showOrders = mode === "orders";
  const orderedItems = orders.slice().sort(compareOrdersNewestFirst);
  const [expandedOrderId, setExpandedOrderId] = useState(orderedItems[0]?.id ?? null);

  useEffect(() => {
    if (!showOrders) {
      return;
    }

    setExpandedOrderId((current) => {
      if (current && orderedItems.some((order) => order.id === current)) {
        return current;
      }

      return orderedItems[0]?.id ?? null;
    });
  }, [orderedItems, showOrders]);

  if (!open) {
    return null;
  }

  return (
    <>
      <button type="button" className="drawer-backdrop" onClick={onClose} aria-label="Close panel" />
      <aside className="activity-drawer">
        <div className="activity-drawer-head">
          <div>
            <p className="eyebrow">Your space</p>
            <h2>{showCart ? "My Cart" : showOrders ? "My Orders" : "Notifications"}</h2>
          </div>
          <div className="drawer-actions">
            <button className="ghost" onClick={onRefresh}>
              Refresh
            </button>
            <button className="ghost" onClick={onClose}>
              Close
            </button>
          </div>
        </div>

        <div className="list-stack">
          {showCart ? (
            <CartContent
              cart={cart}
              onUpdate={onUpdateCart}
              onCheckout={onCheckout}
              checkoutBusy={checkoutBusy}
              checkoutStatus={checkoutStatus}
            />
          ) : showOrders ? (
            orderedItems.length === 0 ? (
              <p className="muted">No orders placed yet.</p>
            ) : (
              orderedItems.map((order) => {
                const workflow = workflows[order.id];
                const payment = payments[order.id];
                const orderNotifications = notifications.filter((item) => item.orderId === order.id);
                const orderSummary = orderItemsSummary(order.items, productsById);
                const isExpanded = expandedOrderId === order.id;

                return (
                  <article className="order-card" key={order.id}>
                    <div className="order-summary-card">
                      <div className="order-summary-grid">
                        <div>
                          <span className="order-summary-label">Product Name</span>
                          <strong>{orderSummary}</strong>
                        </div>
                        <div>
                          <span className="order-summary-label">Quantity</span>
                          <strong>{order.items.reduce((sum, item) => sum + item.quantity, 0)}</strong>
                        </div>
                        <div>
                          <span className="order-summary-label">Order ID</span>
                          <strong>{shortOrderId(order.id)}</strong>
                        </div>
                        <div>
                          <span className="order-summary-label">Order Date</span>
                          <strong>{formatOrderDate(order.createdAt)}</strong>
                        </div>
                        <div>
                          <span className="order-summary-label">Total Amount</span>
                          <strong>{currencyLabel(order.totalAmount, order.currency)}</strong>
                        </div>
                        <div>
                          <span className="order-summary-label">Payment Status</span>
                          <strong>{payment?.status === "SUCCESS" ? "Paid" : payment?.status || order.paymentStatus}</strong>
                        </div>
                        <div className="order-summary-wide">
                          <span className="order-summary-label">Order Status</span>
                          <div className="order-status-line">
                            <span className="status-dot confirmed" />
                            <strong>{order.status === "CONFIRMED" ? "Confirmed" : order.status}</strong>
                          </div>
                        </div>
                      </div>
                    </div>

                    <div className="card-actions">
                      <button
                        className="ghost"
                        onClick={() =>
                          setExpandedOrderId((current) => (current === order.id ? null : order.id))
                        }
                      >
                        {isExpanded ? "Hide Details" : "Track Order"}
                      </button>
                    </div>

                    {isExpanded ? (
                      <>
                        <div className="order-timeline">
                          {orderTimeline(order, workflow, payment).map((step) => (
                            <div
                              key={step.key}
                              className={`timeline-step ${step.active ? "active" : ""} ${step.current ? "current" : ""}`}
                            >
                              <span className="timeline-icon">{step.icon}</span>
                              <span>{step.label}</span>
                            </div>
                          ))}
                        </div>

                        <div className="mini-list">
                          {order.items.map((item) => {
                            const product = productsById[item.productId];
                            return (
                              <div className="order-line-item" key={item.id || `${order.id}-${item.productId}`}>
                                <strong>{product?.name || "Product"}</strong>
                                <span>Qty {item.quantity}</span>
                              </div>
                            );
                          })}
                        </div>

                        <div className="card-actions">
                          <button className="ghost" onClick={onRefresh}>
                            Refresh status
                          </button>
                          {order.status !== "CANCELLED" && order.status !== "FAILED" ? (
                            <button className="danger" onClick={() => onCancel(order.id)}>
                              Cancel order
                            </button>
                          ) : null}
                        </div>

                        {orderNotifications.length > 0 ? (
                          <div className="order-notes">
                            <strong>Order updates</strong>
                            {orderNotifications.map((notification) => (
                              <div className="order-note-item" key={notification.id}>
                                <span className="order-note-type">
                                  {formatNotificationType(notification.type)}
                                </span>
                                <span>{notification.message}</span>
                              </div>
                            ))}
                          </div>
                        ) : null}
                      </>
                    ) : null}
                  </article>
                );
              })
            )
          ) : notifications.length === 0 ? (
            <p className="muted">No notifications yet.</p>
          ) : (
            notifications.map((notification) => (
              <article
                className={`notification-card ${readNotificationIds.has(notification.id) ? "read" : "unread"}`}
                key={notification.id}
                onClick={() => onNotificationSeen(notification.id)}
              >
                <div className="notification-card-head">
                  <strong>{formatNotificationType(notification.type)}</strong>
                  <span className="pill alt">{notification.channel}</span>
                </div>
                <p>{notification.message}</p>
                <div className="notification-card-meta">
                  <span>{notification.status}</span>
                  <span>{notification.recipient}</span>
                </div>
              </article>
            ))
          )}
        </div>
      </aside>
    </>
  );
}

function AdminPanel({ token, products, inventoryByProductId, onCreated, onInventoryUpdated }) {
  const [productForm, setProductForm] = useState({
    sku: "",
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
  const [inventorySearch, setInventorySearch] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const inventoryResults = useMemo(() => {
    const query = inventorySearch.trim().toLowerCase();

    return products
      .filter((product) => {
        if (!query) {
          return true;
        }

        return [product.sku, product.name, product.brand, product.category]
          .filter(Boolean)
          .some((value) => value.toLowerCase().includes(query));
      })
      .slice(0, 8);
  }, [inventorySearch, products]);

  const selectedProduct = useMemo(
    () => products.find((product) => product.id === inventoryForm.productId) ?? null,
    [inventoryForm.productId, products]
  );

  const selectedInventory = selectedProduct ? inventoryByProductId[selectedProduct.id] : null;

  function selectInventoryProduct(product) {
    setInventoryForm((current) => ({
      ...current,
      productId: product.id,
      warehouseLocation: current.warehouseLocation || inventoryByProductId[product.id]?.warehouseLocation || ""
    }));
    setInventorySearch(productCode(product));
  }

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
        sku: "",
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
      setMessage(
        `Inventory updated for ${selectedProduct ? `${productCode(selectedProduct)} | ${selectedProduct.name}` : "product"}.`
      );
      setInventoryForm({
        productId: "",
        quantity: "",
        warehouseLocation: ""
      });
      setInventorySearch("");
      await onInventoryUpdated();
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  return (
    <section className="panel admin-panel">
      <div className="panel-title">
        <h2>Admin operations</h2>
        <span>SKU-first product creation and inventory control</span>
      </div>

      <div className="admin-grid">
        <form className="stacked-form compact" onSubmit={handleCreateProduct}>
          <h3>Create product</h3>
          <input
            placeholder="SKU"
            value={productForm.sku}
            onChange={(event) => setProductForm({ ...productForm, sku: event.target.value })}
            required
          />
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
          <h3>Update inventory</h3>
          <input
            placeholder="Search by SKU, name, brand, or category"
            value={inventorySearch}
            onChange={(event) => setInventorySearch(event.target.value)}
          />
          <div className="inventory-results">
            {inventoryResults.map((product) => {
              const inventory = inventoryByProductId[product.id];
              const isSelected = product.id === inventoryForm.productId;

              return (
                <button
                  key={product.id}
                  className={`inventory-result ${isSelected ? "selected" : ""}`}
                  type="button"
                  onClick={() => selectInventoryProduct(product)}
                >
                  <strong>{productCode(product)}</strong>
                  <span>{product.name}</span>
                  <span>
                    Available: {inventory?.availableQuantity ?? 0} | Reserved: {inventory?.reservedQuantity ?? 0}
                  </span>
                </button>
              );
            })}
          </div>
          {selectedProduct ? (
            <div className="selected-product-card">
              <strong>{selectedProduct.name}</strong>
              <span>{productCode(selectedProduct)}</span>
              <span>Current stock: {selectedInventory?.availableQuantity ?? 0}</span>
              <span>Reserved stock: {selectedInventory?.reservedQuantity ?? 0}</span>
              <span>Warehouse: {selectedInventory?.warehouseLocation || "Not set"}</span>
              <span className="mono-text">Internal ID: {selectedProduct.id}</span>
            </div>
          ) : (
            <p className="muted form-hint">Select a product before submitting a stock update.</p>
          )}
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
          <button type="submit" disabled={!inventoryForm.productId}>
            Add stock
          </button>
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
  const [activeCategory, setActiveCategory] = useState("All");
  const [sortBy, setSortBy] = useState("featured");
  const [status, setStatus] = useState("");
  const [error, setError] = useState("");
  const [checkoutBusy, setCheckoutBusy] = useState(false);
  const [profileOpen, setProfileOpen] = useState(false);
  const [activityDrawer, setActivityDrawer] = useState(null);
  const [readNotificationIds, setReadNotificationIds] = useState(new Set());

  const deferredSearch = useDeferredValue(search);
  const displayName = session.displayName?.trim() || session.userId;
  const displayRole = formatRole(session.role) || "User";

  const categories = useMemo(
    () => ["All", ...new Set(products.map((product) => product.category).filter(Boolean))],
    [products]
  );

  const productsById = useMemo(
    () =>
      products.reduce((accumulator, product) => {
        accumulator[product.id] = product;
        return accumulator;
      }, {}),
    [products]
  );

  const visibleProducts = useMemo(() => {
    const query = deferredSearch.trim().toLowerCase();
    const filtered = products.filter((product) => {
      const matchesSearch =
        !query ||
        [product.sku, product.name, product.description, product.category, product.brand]
          .filter(Boolean)
          .some((value) => value.toLowerCase().includes(query));

      const matchesCategory =
        activeCategory === "All" || (product.category || "General") === activeCategory;

      return matchesSearch && matchesCategory;
    });

    const ranked = [...filtered];
    ranked.sort((left, right) => {
      const leftInventory = inventoryByProductId[left.id];
      const rightInventory = inventoryByProductId[right.id];
      const leftStock = leftInventory?.availableQuantity ?? 0;
      const rightStock = rightInventory?.availableQuantity ?? 0;

      switch (sortBy) {
        case "price-asc":
          return Number(left.price) - Number(right.price);
        case "price-desc":
          return Number(right.price) - Number(left.price);
        case "stock-desc":
          return rightStock - leftStock;
        case "name-asc":
          return left.name.localeCompare(right.name);
        default:
          return rightStock - leftStock || left.name.localeCompare(right.name);
      }
    });

    return ranked;
  }, [activeCategory, deferredSearch, inventoryByProductId, products, sortBy]);

  const featuredProducts = useMemo(() => visibleProducts.slice(0, 3), [visibleProducts]);
  const sortedOrders = useMemo(() => [...orders].sort(compareOrdersNewestFirst), [orders]);
  const recentOrders = useMemo(() => sortedOrders.slice(0, 3), [sortedOrders]);
  const cartCount = useMemo(() => cart.reduce((sum, item) => sum + item.quantity, 0), [cart]);
  const unreadNotificationCount = useMemo(
    () => notifications.filter((notification) => !readNotificationIds.has(notification.id)).length,
    [notifications, readNotificationIds]
  );
  const inStockCount = useMemo(
    () => products.filter((product) => (inventoryByProductId[product.id]?.availableQuantity ?? 0) > 0).length,
    [inventoryByProductId, products]
  );

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
      setReadNotificationIds((current) => {
        const next = new Set();
        nextNotifications.forEach((notification) => {
          if (current.has(notification.id)) {
            next.add(notification.id);
          }
        });
        return next;
      });
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
      setActivityDrawer("orders");
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

  function openCartDrawer() {
    setProfileOpen(false);
    setActivityDrawer("cart");
  }

  function openOrdersDrawer() {
    setProfileOpen(false);
    setActivityDrawer("orders");
  }

  function openNotificationsDrawer() {
    setProfileOpen(false);
    setReadNotificationIds(new Set(notifications.map((notification) => notification.id)));
    setActivityDrawer("notifications");
  }

  function markNotificationRead(notificationId) {
    setReadNotificationIds((current) => new Set(current).add(notificationId));
  }

  return (
    <div className="app-shell marketplace-shell">
      <MarketplaceHero
        displayName={displayName}
        displayRole={displayRole}
        totalProducts={products.length}
        categoriesCount={Math.max(categories.length - 1, 0)}
        inStockCount={inStockCount}
        search={search}
        onSearchChange={setSearch}
        unreadNotificationCount={unreadNotificationCount}
        cartCount={cartCount}
        recentOrders={recentOrders}
        onOpenCart={openCartDrawer}
        onOpenNotifications={openNotificationsDrawer}
        onOpenOrders={openOrdersDrawer}
        profileOpen={profileOpen}
        onToggleProfile={() => setProfileOpen((current) => !current)}
        logout={logout}
      />

      {status ? <p className="success-text banner-text">{status}</p> : null}
      {error ? <p className="error-text banner-text">{error}</p> : null}

      <MerchRail
        featuredProducts={featuredProducts}
        inventoryByProductId={inventoryByProductId}
        onSelectCategory={setActiveCategory}
      />

      <CatalogToolbar
        categories={categories}
        activeCategory={activeCategory}
        onCategoryChange={setActiveCategory}
        sortBy={sortBy}
        onSortChange={setSortBy}
        resultCount={visibleProducts.length}
      />

      <section className="catalog-grid">
        {visibleProducts.map((product) => (
          <ProductCard
            key={product.id}
            product={product}
            inventory={inventoryByProductId[product.id]}
            onAdd={addToCart}
          />
        ))}
      </section>

      <ActivityDrawer
        mode={activityDrawer}
        open={Boolean(activityDrawer)}
        cart={cart}
        orders={orders}
        notifications={notifications}
        readNotificationIds={readNotificationIds}
        productsById={productsById}
        workflows={workflows}
        payments={payments}
        checkoutBusy={checkoutBusy}
        checkoutStatus={status.includes("placed successfully") ? status : ""}
        onUpdateCart={updateCartQuantity}
        onCheckout={handleCheckout}
        onRefresh={() => Promise.all([loadOrders(), loadNotifications(), loadProducts()])}
        onCancel={handleCancel}
        onNotificationSeen={markNotificationRead}
        onClose={() => setActivityDrawer(null)}
      />

      {isAdmin ? (
        <AdminPanel
          token={session.accessToken}
          products={products}
          inventoryByProductId={inventoryByProductId}
          onCreated={loadProducts}
          onInventoryUpdated={loadProducts}
        />
      ) : null}
    </div>
  );
}
