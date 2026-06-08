const state = {
    userId: "2",
    method: "ALIPAY",
    products: [],
    cart: { lines: [], totalAmount: 0 },
    orders: []
};

const els = {
    productGrid: document.querySelector("#productGrid"),
    cartList: document.querySelector("#cartList"),
    orderList: document.querySelector("#orderList"),
    activityLog: document.querySelector("#activityLog"),
    productCount: document.querySelector("#productCount"),
    cartCount: document.querySelector("#cartCount"),
    orderCount: document.querySelector("#orderCount"),
    cartTotal: document.querySelector("#cartTotal"),
    userBadge: document.querySelector("#userBadge"),
    headerPreview: document.querySelector("#headerPreview")
};

function headers() {
    return {
        "Content-Type": "application/json",
        "X-User-Id": state.userId
    };
}

async function api(path, options = {}) {
    const response = await fetch(path, {
        ...options,
        headers: {
            ...headers(),
            ...(options.headers || {})
        }
    });
    const payload = await response.json();
    if (!payload.success) {
        throw new Error(payload.message || "请求失败");
    }
    return payload.data;
}

function money(value) {
    return `¥${Number(value || 0).toFixed(2)}`;
}

function emptyNode() {
    return document.querySelector("#emptyTemplate").content.cloneNode(true);
}

function log(message) {
    const item = document.createElement("li");
    item.innerHTML = `<span>${message}</span><time>${new Date().toLocaleTimeString()}</time>`;
    els.activityLog.prepend(item);
    while (els.activityLog.children.length > 6) {
        els.activityLog.lastElementChild.remove();
    }
}

function renderProducts() {
    els.productGrid.innerHTML = "";
    state.products.forEach((product) => {
        const card = document.createElement("article");
        card.className = "product-card";
        card.innerHTML = `
            <div class="product-art">${product.name.slice(0, 1)}</div>
            <div>
                <h3>${product.name}</h3>
                <p>${product.description}</p>
            </div>
            <div class="product-meta">
                <span class="price">${money(product.price)}</span>
                <span class="stock">库存 ${product.stock}</span>
            </div>
            <button class="primary-button" data-add="${product.id}">加入购物车</button>
        `;
        els.productGrid.appendChild(card);
    });
    els.productCount.textContent = state.products.length;
}

function renderCart() {
    els.cartList.innerHTML = "";
    if (!state.cart.lines.length) {
        els.cartList.appendChild(emptyNode());
    }
    state.cart.lines.forEach((line) => {
        const item = document.createElement("div");
        item.className = "line-item";
        item.innerHTML = `
            <strong>${line.productName}</strong>
            <button class="ghost-button" data-remove="${line.productId}" title="删除">移除</button>
            <small>${line.quantity} 件 x ${money(line.price)}</small>
            <strong>${money(line.lineTotal)}</strong>
        `;
        els.cartList.appendChild(item);
    });
    els.cartTotal.textContent = money(state.cart.totalAmount);
    els.cartCount.textContent = state.cart.lines.reduce((sum, line) => sum + line.quantity, 0);
}

function renderOrders() {
    els.orderList.innerHTML = "";
    if (!state.orders.length) {
        els.orderList.appendChild(emptyNode());
    }
    state.orders.forEach((order) => {
        const item = document.createElement("div");
        item.className = "line-item";
        item.innerHTML = `
            <strong>订单 #${order.id}</strong>
            <span class="status-pill">${order.status}</span>
            <small>${order.items.length} 个商品</small>
            <strong>${money(order.totalAmount)}</strong>
        `;
        els.orderList.appendChild(item);
    });
    els.orderCount.textContent = state.orders.length;
}

function renderUser() {
    const isAdmin = state.userId === "1";
    els.userBadge.textContent = isAdmin ? "admin" : "alice";
    els.headerPreview.textContent = `X-User-Id: ${state.userId}`;
    document.querySelectorAll(".segment").forEach((button) => {
        button.classList.toggle("active", button.dataset.user === state.userId);
    });
}

function renderPayments() {
    document.querySelectorAll(".pay-method").forEach((button) => {
        button.classList.toggle("active", button.dataset.method === state.method);
    });
}

async function loadProducts() {
    state.products = await api("/api/products", { headers: { "X-User-Id": state.userId } });
    renderProducts();
}

async function loadCart() {
    state.cart = await api("/api/cart");
    renderCart();
}

async function loadOrders() {
    state.orders = await api("/api/orders");
    renderOrders();
}

async function refreshAll() {
    renderUser();
    renderPayments();
    await Promise.all([loadProducts(), loadCart(), loadOrders()]);
}

async function addToCart(productId) {
    state.cart = await api("/api/cart", {
        method: "POST",
        body: JSON.stringify({ productId: Number(productId), quantity: 1 })
    });
    renderCart();
    log(`商品 #${productId} 已加入购物车`);
}

async function removeFromCart(productId) {
    state.cart = await api(`/api/cart/${productId}`, { method: "DELETE" });
    renderCart();
    log(`商品 #${productId} 已从购物车移除`);
}

async function createOrder() {
    const order = await api("/api/orders", { method: "POST" });
    log(`订单 #${order.id} 已生成`);
    await refreshAll();
}

async function payLatestOrder() {
    const order = state.orders.find((item) => item.status === "CREATED");
    if (!order) {
        throw new Error("没有待支付订单");
    }
    const payment = await api("/api/payments", {
        method: "POST",
        body: JSON.stringify({ orderId: order.id, method: state.method })
    });
    await api("/api/payments/callback", {
        method: "POST",
        body: JSON.stringify({ tradeNo: payment.tradeNo, success: true, channelMessage: "front mock success" })
    });
    log(`订单 #${order.id} 使用 ${state.method} 支付成功`);
    await refreshAll();
}

async function shipLatestOrder() {
    const order = state.orders.find((item) => item.status === "PAID");
    if (!order) {
        throw new Error("没有待发货订单");
    }
    const oldUser = state.userId;
    state.userId = "1";
    await api(`/api/orders/${order.id}/ship`, { method: "PATCH" });
    state.userId = oldUser;
    log(`管理员已为订单 #${order.id} 发货`);
    await refreshAll();
}

async function receiveLatestOrder() {
    const order = state.orders.find((item) => item.status === "SHIPPED");
    if (!order) {
        throw new Error("没有待收货订单");
    }
    await api(`/api/orders/${order.id}/receive`, { method: "PATCH" });
    log(`订单 #${order.id} 已收货`);
    await refreshAll();
}

async function run(action) {
    try {
        await action();
    } catch (error) {
        log(error.message);
    }
}

document.addEventListener("click", (event) => {
    const addId = event.target.dataset.add;
    const removeId = event.target.dataset.remove;
    const user = event.target.dataset.user;
    const method = event.target.dataset.method;

    if (addId) run(() => addToCart(addId));
    if (removeId) run(() => removeFromCart(removeId));
    if (user) {
        state.userId = user;
        run(refreshAll);
    }
    if (method) {
        state.method = method;
        renderPayments();
    }
});

document.querySelector("#refreshBtn").addEventListener("click", () => run(refreshAll));
document.querySelector("#createOrderBtn").addEventListener("click", () => run(createOrder));
document.querySelector("#payBtn").addEventListener("click", () => run(payLatestOrder));
document.querySelector("#shipBtn").addEventListener("click", () => run(shipLatestOrder));
document.querySelector("#receiveBtn").addEventListener("click", () => run(receiveLatestOrder));

refreshAll().then(() => log("前端已连接后端服务"));
