
import { useState } from "react";
import "./App.css";

function App() {
  const [activePage, setActivePage] = useState("Overview");
  const [searchQuery, setSearchQuery] = useState("");

  const menuItems = [
    "Overview",
    "Environments",
    "Schema Comparison",
    "Migration Planner",
    "CDC Monitoring",
    "Migration History",
  ];

  const icons = ["◫", "▤", "⇄", "⚙", "⌁", "◷"];

  const environments = [
    {
      name: "Source Database",
      type: "PostgreSQL",
      role: "Source",
      status: "Configured",
    },
    {
      name: "Target Database",
      type: "PostgreSQL",
      role: "Target",
      status: "Configured",
    },
  ];

  const pageDescriptions = {
    Overview: "Monitor your migration workspace",
    Environments: "Manage your source and target databases",
    "Schema Comparison": "Compare source and target database schemas",
    "Migration Planner": "Plan database migration operations",
    "CDC Monitoring": "Monitor database synchronization",
    "Migration History": "Review schema and migration history",
  };

  const renderOverview = () => (
    <>
      <section className="section-heading">
        <div>
          <h2>Platform Overview</h2>
          <p>Database migration workspace summary</p>
        </div>

        <span className="status-label">
          <span className="online-dot"></span>
          System Online
        </span>
      </section>

      <section className="stats-grid">
        <div className="stat-card">
          <div className="stat-card-top">
            <div className="stat-icon indigo">▤</div>
            <span className="metric-status">Ready</span>
          </div>
          <p>Total Environments</p>
          <h2>02</h2>
          <small>Configured databases</small>
        </div>

        <div className="stat-card">
          <div className="stat-card-top">
            <div className="stat-icon blue">⇄</div>
            <span className="metric-status neutral">Idle</span>
          </div>
          <p>Schema Comparisons</p>
          <h2>00</h2>
          <small>Comparisons completed</small>
        </div>

        <div className="stat-card">
          <div className="stat-card-top">
            <div className="stat-icon orange">⚙</div>
            <span className="metric-status neutral">Idle</span>
          </div>
          <p>Migration Plans</p>
          <h2>00</h2>
          <small>Plans generated</small>
        </div>

        <div className="stat-card">
          <div className="stat-card-top">
            <div className="stat-icon teal">⌁</div>
            <span className="metric-status neutral">Idle</span>
          </div>
          <p>CDC Events</p>
          <h2>00</h2>
          <small>Events processed</small>
        </div>
      </section>

      <section className="overview-grid">
        <div className="panel">
          <div className="panel-header">
            <div>
              <h3>Database Environments</h3>
              <p>Configured database connections</p>
            </div>

            <button onClick={() => setActivePage("Environments")}>
              View All →
            </button>
          </div>

          {environments.map((env) => (
            <div className="environment-row" key={env.name}>
              <div className="db-icon">DB</div>

              <div className="environment-info">
                <strong>{env.name}</strong>
                <p>{env.type} · {env.role}</p>
              </div>

              <span className="configured">{env.status}</span>
            </div>
          ))}
        </div>

        <div className="panel">
          <div className="panel-header">
            <div>
              <h3>Migration Activity</h3>
              <p>Latest workspace activity</p>
            </div>
          </div>

          <div className="empty-state">
            <span>◷</span>
            <strong>No migration activity yet</strong>
            <p>Migration activities will appear here.</p>
          </div>
        </div>

        <div className="panel">
          <div className="panel-header">
            <div>
              <h3>Migration Status</h3>
              <p>Current migration state</p>
            </div>
          </div>

          <div className="status-summary">
            <div>
              <span className="status-marker active-marker"></span>
              <p>Active</p>
              <strong>0</strong>
            </div>
            <div>
              <span className="status-marker complete-marker"></span>
              <p>Completed</p>
              <strong>0</strong>
            </div>
            <div>
              <span className="status-marker failed-marker"></span>
              <p>Failed</p>
              <strong>0</strong>
            </div>
          </div>
        </div>
      </section>

      <section className="panel quick-actions">
        <div className="panel-header">
          <div>
            <h3>Quick Actions</h3>
            <p>Start working with your databases</p>
          </div>
        </div>

        <div className="actions-grid">
          <button onClick={() => setActivePage("Environments")}>
            <span>＋</span>
            <strong>Manage Environments</strong>
            <small>Configure database connections</small>
          </button>

          <button onClick={() => setActivePage("Schema Comparison")}>
            <span>⇄</span>
            <strong>Compare Schemas</strong>
            <small>Find schema differences</small>
          </button>

          <button onClick={() => setActivePage("Migration Planner")}>
            <span>⚙</span>
            <strong>Migration Planner</strong>
            <small>Generate migration operations</small>
          </button>
        </div>
      </section>
    </>
  );

  const renderEnvironments = () => (
    <>
      <section className="section-heading">
        <div>
          <h2>Database Environments</h2>
          <p>Manage your source and target databases.</p>
        </div>
      </section>

      <section className="content-grid">
        {environments.map((env) => (
          <div className="panel" key={env.name}>
            <div className="panel-header">
              <div>
                <h3>{env.name}</h3>
                <p>{env.role} database environment</p>
              </div>

              <span className="configured">{env.status}</span>
            </div>

            <div className="environment-row">
              <div className="db-icon">DB</div>

              <div className="environment-info">
                <strong>{env.type}</strong>
                <p>{env.role} Environment</p>
              </div>
            </div>

            <div className="environment-details">
              <p><strong>Database Type:</strong> {env.type}</p>
              <p><strong>Environment:</strong> {env.role}</p>
              <p><strong>Status:</strong> {env.status}</p>
            </div>
          </div>
        ))}
      </section>

      <section className="panel">
        <h3>Environment Configuration</h3>
        <p>
          Database connection configuration and connection
          testing will be implemented in the next development phase.
        </p>
      </section>
    </>
  );

  const renderPlaceholder = (page) => {
    const descriptions = {
      "Schema Comparison":
        "Compare source and target database schemas and identify structural differences.",
      "Migration Planner":
        "Generate migration operations based on detected schema differences.",
      "CDC Monitoring":
        "Monitor Change Data Capture events and track database synchronization.",
      "Migration History":
        "Review previous schema comparisons and migration execution history.",
    };

    return (
      <section className="placeholder-panel panel">
        <div className="section-heading placeholder-heading">
          <h2>{page}</h2>
          <p>{descriptions[page]}</p>
        </div>

        <div className="empty-state">
          <span aria-hidden="true">⚙</span>
          <strong>{page} module</strong>
          <p>This module is under development.</p>
        </div>
      </section>
    );
  };

  const renderPage = () => {
    switch (activePage) {
      case "Overview":
        return renderOverview();

      case "Environments":
        return renderEnvironments();

      case "Schema Comparison":
      case "Migration Planner":
      case "CDC Monitoring":
      case "Migration History":
        return renderPlaceholder(activePage);

      default:
        return renderOverview();
    }
  };

  return (
    <div className="app">
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-logo">S</div>
          <div>
            <h2>ShadowBase</h2>
            <p>Migration Platform</p>
          </div>
        </div>

        <p className="menu-title">WORKSPACE</p>

        <nav>
          {menuItems.map((item, index) => (
            <button
              key={item}
              type="button"
              aria-current={activePage === item ? "page" : undefined}
              className={
                activePage === item
                  ? "nav-item active"
                  : "nav-item"
              }
              onClick={() => setActivePage(item)}
            >
              <span>{icons[index]}</span>
              {item}
            </button>
          ))}
        </nav>

        <div className="system-status">
          <span className="online-dot"></span>
          System Online
        </div>
      </aside>

      <main className="main-content">
        <header className="topbar">
          <div>
            <p className="breadcrumb">
              Workspace / {activePage}
            </p>
            <h1>{activePage}</h1>
            <p className="page-description">{pageDescriptions[activePage]}</p>
          </div>

          <div className="topbar-actions">
            <label className="search-control">
              <span className="search-icon" aria-hidden="true">⌕</span>
              <span className="sr-only">Search workspace</span>
              <input
                type="search"
                placeholder="Search"
                value={searchQuery}
                onChange={(event) => setSearchQuery(event.target.value)}
              />
            </label>
            <button className="icon-button" type="button" aria-label="Notifications">
              <span aria-hidden="true">◷</span>
              <span className="notification-dot"></span>
            </button>
            <div className="user-profile">
              <div className="avatar">A</div>
              <div>
                <strong>Admin</strong>
                <p>Developer</p>
              </div>
            </div>
          </div>
        </header>

        {renderPage()}

        <footer>
          ShadowBase © 2026 | Zero-Downtime Schema Migration Platform
        </footer>
      </main>
    </div>
  );
}

export default App;
