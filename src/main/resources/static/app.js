const state = {
  token: localStorage.getItem("token"),
  user: JSON.parse(localStorage.getItem("user") || "null"),
  projects: [],
  tasks: [],
  users: []
};

const $ = (selector) => document.querySelector(selector);

function setMessage(target, message, ok = false) {
  const el = $(target);
  el.textContent = message || "";
  el.style.color = ok ? "#16685a" : "#b3261e";
}

async function api(path, options = {}) {
  const response = await fetch(path, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(state.token ? { Authorization: `Bearer ${state.token}` } : {}),
      ...(options.headers || {})
    }
  });

  if (!response.ok) {
    let message = "Request failed";
    try {
      const body = await response.json();
      message = body.message || message;
    } catch {
      message = response.statusText || message;
    }
    throw new Error(message);
  }

  if (response.status === 204) {
    return null;
  }
  return response.json();
}

function showAuth(mode) {
  $("#authView").classList.toggle("hidden", !!state.token);
  $("#appView").classList.toggle("hidden", !state.token);
  $("#loginForm").classList.toggle("hidden", mode === "signup");
  $("#signupForm").classList.toggle("hidden", mode !== "signup");
  $("#loginTab").classList.toggle("active", mode !== "signup");
  $("#signupTab").classList.toggle("active", mode === "signup");
}

function saveSession(auth) {
  state.token = auth.token;
  state.user = { id: auth.id, name: auth.name, email: auth.email, role: auth.role };
  localStorage.setItem("token", state.token);
  localStorage.setItem("user", JSON.stringify(state.user));
}

function logout() {
  state.token = null;
  state.user = null;
  localStorage.removeItem("token");
  localStorage.removeItem("user");
  showAuth("login");
}

async function login(event) {
  event.preventDefault();
  const data = Object.fromEntries(new FormData(event.target));
  try {
    saveSession(await api("/api/auth/login", { method: "POST", body: JSON.stringify(data) }));
    event.target.reset();
    await loadApp();
  } catch (error) {
    setMessage("#authMessage", error.message);
  }
}

async function signup(event) {
  event.preventDefault();
  const data = Object.fromEntries(new FormData(event.target));
  try {
    saveSession(await api("/api/auth/signup", { method: "POST", body: JSON.stringify(data) }));
    event.target.reset();
    await loadApp();
  } catch (error) {
    setMessage("#authMessage", error.message);
  }
}

async function loadApp() {
  showAuth("login");
  $("#userBadge").textContent = `${state.user.name} - ${state.user.role}`;
  document.querySelectorAll(".admin-only").forEach((el) => {
    el.classList.toggle("hidden", state.user.role !== "ADMIN");
  });

  try {
    const [dashboard, projects, tasks, users] = await Promise.all([
      api("/api/dashboard"),
      api("/api/projects"),
      api("/api/tasks"),
      api("/api/users")
    ]);
    renderDashboard(dashboard);
    state.projects = projects;
    state.tasks = tasks;
    state.users = users;
    renderProjects();
    renderTasks();
    renderUsers();
    fillSelects();
    setMessage("#appMessage", "");
  } catch (error) {
    setMessage("#appMessage", error.message);
    if (error.message.includes("JWT") || error.message.includes("Unauthorized")) {
      logout();
    }
  }
}

function renderDashboard(dashboard) {
  $("#openCount").textContent = dashboard.assignedOpen;
  $("#doneCount").textContent = dashboard.completed;
  $("#overdueCount").textContent = dashboard.overdue;
  $("#projectCount").textContent = dashboard.visibleProjects;
  $("#projectTotal").textContent = dashboard.visibleProjects;
  $("#taskTotal").textContent = dashboard.visibleTasks;
}

function renderProjects() {
  $("#projects").innerHTML = state.projects.map((project) => `
    <article class="item">
      <h3>${escapeHtml(project.name)}</h3>
      <p>${escapeHtml(project.description || "No description")}</p>
      <div class="meta">
        <span>Owner: ${escapeHtml(project.owner?.name || "Unknown")}</span>
        <span>${project.members.length} member(s)</span>
      </div>
    </article>
  `).join("") || `<p class="message">No projects yet.</p>`;
}

function renderTasks() {
  $("#tasks").innerHTML = state.tasks.map((task) => {
    const assigned = task.assignedTo ? task.assignedTo.name : "Unassigned";
    const canUpdate = state.user.role === "ADMIN" || task.assignedTo?.id === state.user.id;
    return `
      <article class="task ${task.overdue ? "overdue" : ""} ${task.status === "DONE" ? "done" : ""}">
        <h3>${escapeHtml(task.title)}</h3>
        <p>${escapeHtml(task.description || "No details")}</p>
        <div class="meta">
          <span>${escapeHtml(task.projectName)}</span>
          <span>Assigned: ${escapeHtml(assigned)}</span>
          <span>Due: ${task.dueDate || "Not set"}</span>
          ${task.overdue ? "<span>Overdue</span>" : ""}
        </div>
        ${canUpdate ? statusSelect(task) : `<div class="meta"><span>${task.status.replace("_", " ")}</span></div>`}
      </article>
    `;
  }).join("") || `<p class="message">No tasks yet.</p>`;
}

function statusSelect(task) {
  const colors = {
    TODO: "background: var(--todo-bg); color: var(--todo-fg); border-color: var(--todo-border);",
    IN_PROGRESS: "background: var(--progress-bg); color: var(--progress-fg); border-color: var(--progress-border);",
    DONE: "background: var(--done-bg); color: var(--done-fg); border-color: var(--done-border);"
  };
  const style = colors[task.status] || "";
  return `
    <div class="meta">
      <select class="status-select" data-task-id="${task.id}" style="${style}">
        ${["TODO", "IN_PROGRESS", "DONE"].map((status) => `
          <option value="${status}" ${task.status === status ? "selected" : ""}>${status.replace("_", " ")}</option>
        `).join("")}
      </select>
    </div>
  `;
}

function renderUsers() {
  $("#users").innerHTML = state.users.map((user) => `
    <article class="item">
      <h3>${escapeHtml(user.name)}</h3>
      <p>${escapeHtml(user.email)}</p>
      <div class="meta"><span>${user.role}</span></div>
    </article>
  `).join("");
}

function fillSelects() {
  const projectOptions = state.projects.map((project) => `<option value="${project.id}">${escapeHtml(project.name)}</option>`).join("");
  document.querySelectorAll('select[name="projectId"]').forEach((select) => {
    select.innerHTML = projectOptions || `<option value="">Create a project first</option>`;
  });

  const userOptions = `<option value="">Unassigned</option>` + state.users
    .map((user) => `<option value="${user.id}">${escapeHtml(user.name)} (${user.role})</option>`)
    .join("");
  const requiredUserOptions = state.users
    .map((user) => `<option value="${user.id}">${escapeHtml(user.name)} (${user.role})</option>`)
    .join("");
  $('select[name="assignedToId"]').innerHTML = userOptions;
  $('select[name="userId"]').innerHTML = requiredUserOptions;
}

async function createProject(event) {
  event.preventDefault();
  const data = Object.fromEntries(new FormData(event.target));
  try {
    await api("/api/projects", { method: "POST", body: JSON.stringify(data) });
    event.target.reset();
    await loadApp();
    setMessage("#appMessage", "Project created", true);
  } catch (error) {
    setMessage("#appMessage", error.message);
  }
}

async function createTask(event) {
  event.preventDefault();
  const data = Object.fromEntries(new FormData(event.target));
  data.projectId = Number(data.projectId);
  data.assignedToId = data.assignedToId ? Number(data.assignedToId) : null;
  data.dueDate = data.dueDate || null;
  try {
    await api("/api/tasks", { method: "POST", body: JSON.stringify(data) });
    event.target.reset();
    await loadApp();
    setMessage("#appMessage", "Task created", true);
  } catch (error) {
    setMessage("#appMessage", error.message);
  }
}

async function addMember(event) {
  event.preventDefault();
  const data = Object.fromEntries(new FormData(event.target));
  try {
    await api(`/api/projects/${data.projectId}/members`, {
      method: "POST",
      body: JSON.stringify({ userId: Number(data.userId) })
    });
    await loadApp();
    setMessage("#appMessage", "Member added", true);
  } catch (error) {
    setMessage("#appMessage", error.message);
  }
}

async function updateStatus(event) {
  if (!event.target.matches(".status-select")) {
    return;
  }
  try {
    await api(`/api/tasks/${event.target.dataset.taskId}/status`, {
      method: "PATCH",
      body: JSON.stringify({ status: event.target.value })
    });
    await loadApp();
  } catch (error) {
    setMessage("#appMessage", error.message);
  }
}

function escapeHtml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

$("#loginTab").addEventListener("click", () => showAuth("login"));
$("#signupTab").addEventListener("click", () => showAuth("signup"));
$("#loginForm").addEventListener("submit", login);
$("#signupForm").addEventListener("submit", signup);
$("#logoutBtn").addEventListener("click", logout);
$("#projectForm").addEventListener("submit", createProject);
$("#taskForm").addEventListener("submit", createTask);
$("#memberForm").addEventListener("submit", addMember);
$("#tasks").addEventListener("change", updateStatus);

if (state.token && state.user) {
  loadApp();
} else {
  showAuth("login");
}
