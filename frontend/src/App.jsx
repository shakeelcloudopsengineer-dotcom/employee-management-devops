import { useEffect, useMemo, useState } from 'react';
import {
  createEmployee,
  deleteEmployee,
  getApiErrorMessage,
  getEmployees,
  getEmployeesByDepartment,
  getEmployeesByName,
  updateEmployee,
} from './api/employees.js';

const emptyEmployee = {
  firstName: '',
  lastName: '',
  email: '',
  department: '',
  jobTitle: '',
  salary: '',
  phoneNumber: '',
  joiningDate: '',
  employmentStatus: 'ACTIVE',
};

const departments = ['Engineering', 'Design', 'Human Resources', 'Finance', 'Marketing', 'Operations'];

function initials(employee) {
  return `${employee.firstName?.[0] || ''}${employee.lastName?.[0] || ''}`.toUpperCase();
}

function formatCurrency(value) {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    maximumFractionDigits: 0,
  }).format(value || 0);
}

function formatDate(value) {
  if (!value) return '—';
  return new Intl.DateTimeFormat('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
    .format(new Date(`${value}T00:00:00`));
}

export default function App() {
  const [employees, setEmployees] = useState([]);
  const [rows, setRows] = useState([]);
  const [search, setSearch] = useState('');
  const [department, setDepartment] = useState('');
  const [refreshKey, setRefreshKey] = useState(0);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingEmployee, setEditingEmployee] = useState(null);
  const [form, setForm] = useState(emptyEmployee);

  useEffect(() => {
    let active = true;
    getEmployees()
      .then((data) => {
        if (active) setEmployees(data);
      })
      .catch((requestError) => {
        if (active) setError(getApiErrorMessage(requestError));
      });
    return () => {
      active = false;
    };
  }, [refreshKey]);

  useEffect(() => {
    let active = true;
    setLoading(true);
    setError('');
    const timer = window.setTimeout(async () => {
      try {
        let data;
        if (department) {
          data = await getEmployeesByDepartment(department);
          if (search.trim()) {
            const query = search.trim().toLocaleLowerCase();
            data = data.filter((employee) =>
              `${employee.firstName} ${employee.lastName}`.toLocaleLowerCase().includes(query),
            );
          }
        } else if (search.trim()) {
          data = await getEmployeesByName(search.trim());
        } else {
          data = await getEmployees();
        }
        if (active) setRows(data);
      } catch (requestError) {
        if (active) setError(getApiErrorMessage(requestError));
      } finally {
        if (active) setLoading(false);
      }
    }, search.trim() ? 250 : 0);
    return () => {
      active = false;
      window.clearTimeout(timer);
    };
  }, [search, department, refreshKey]);

  const activeCount = useMemo(
    () => employees.filter((employee) => employee.employmentStatus === 'ACTIVE').length,
    [employees],
  );
  const departmentCount = useMemo(
    () => new Set(employees.map((employee) => employee.department)).size,
    [employees],
  );
  const departmentOptions = useMemo(
    () => [...new Set([...departments, ...employees.map((employee) => employee.department)])].sort(),
    [employees],
  );

  function openCreateDialog() {
    setEditingEmployee(null);
    setForm(emptyEmployee);
    setDialogOpen(true);
  }

  function openEditDialog(employee) {
    setEditingEmployee(employee);
    setForm({
      firstName: employee.firstName,
      lastName: employee.lastName,
      email: employee.email,
      department: employee.department,
      jobTitle: employee.jobTitle,
      salary: employee.salary,
      phoneNumber: employee.phoneNumber || '',
      joiningDate: employee.joiningDate,
      employmentStatus: employee.employmentStatus,
    });
    setDialogOpen(true);
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setSaving(true);
    setError('');
    const payload = { ...form, salary: Number(form.salary) };
    try {
      if (editingEmployee) {
        await updateEmployee(editingEmployee.id, payload);
      } else {
        await createEmployee(payload);
      }
      setDialogOpen(false);
      setRefreshKey((key) => key + 1);
    } catch (requestError) {
      setError(getApiErrorMessage(requestError));
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(employee) {
    if (!window.confirm(`Delete ${employee.firstName} ${employee.lastName}? This cannot be undone.`)) return;
    setError('');
    try {
      await deleteEmployee(employee.id);
      setRefreshKey((key) => key + 1);
    } catch (requestError) {
      setError(getApiErrorMessage(requestError));
    }
  }

  function updateField(event) {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <a className="brand" href="/" aria-label="PeopleDesk home">
          <span className="brand-mark">P</span>
          <span>people<span className="brand-light">desk</span></span>
        </a>
        <div className="workspace-label">WORKSPACE</div>
        <nav className="side-nav" aria-label="Main navigation">
          <a className="nav-item active" href="/">
            <span className="nav-icon">▦</span> Overview
          </a>
          <a className="nav-item" href="#employees">
            <span className="nav-icon">♙</span> Employees
          </a>
        </nav>
        <div className="sidebar-bottom">
          <div className="help-card">
            <div className="help-icon">?</div>
            <strong>Need a hand?</strong>
            <span>Reach out to your workspace admin.</span>
            <a href="mailto:support@example.com">Get support <span>↗</span></a>
          </div>
          <div className="profile">
            <div className="profile-avatar">SA</div>
            <div className="profile-copy"><strong>Shakeel Ahmed</strong><span>Administrator</span></div>
            <span className="profile-more">···</span>
          </div>
        </div>
      </aside>

      <main className="main-content">
        <header className="topbar">
          <div className="breadcrumb">Workspace <span>/</span> <strong>Overview</strong></div>
          <div className="topbar-actions">
            <span className="today-label">{new Intl.DateTimeFormat('en-US', {
              weekday: 'short', month: 'short', day: 'numeric',
            }).format(new Date())}</span>
            <button className="icon-button notification-button" aria-label="Notifications">♧<i /></button>
            <div className="top-avatar">SA</div>
          </div>
        </header>

        <section className="page-content">
          <div className="page-heading">
            <div>
              <p className="eyebrow">PEOPLE OPERATIONS</p>
              <h1>Employee overview</h1>
              <p className="subtitle">Manage your people and keep your team moving forward.</p>
            </div>
            <button className="primary-button" onClick={openCreateDialog}><span>＋</span> Add employee</button>
          </div>

          {error && (
            <div className="alert" role="alert">
              <span className="alert-symbol">!</span>
              <span>{error}</span>
              <button onClick={() => setError('')} aria-label="Dismiss error">×</button>
            </div>
          )}

          <section className="stats-grid" aria-label="Employee statistics">
            <article className="stat-card">
              <div className="stat-top"><span>Total employees</span><span className="stat-icon violet">♙</span></div>
              <div className="stat-number">{employees.length}<span className="stat-unit"> people</span></div>
              <div className="stat-note"><span className="trend">↗</span> Your whole team, in one place</div>
            </article>
            <article className="stat-card">
              <div className="stat-top"><span>Active employees</span><span className="stat-icon green">✓</span></div>
              <div className="stat-number">{activeCount}<span className="stat-unit"> people</span></div>
              <div className="stat-note"><span className="status-dot" /> Currently active</div>
            </article>
            <article className="stat-card">
              <div className="stat-top"><span>Departments</span><span className="stat-icon amber">▤</span></div>
              <div className="stat-number">{departmentCount}<span className="stat-unit"> teams</span></div>
              <div className="stat-note">Across your organization</div>
            </article>
          </section>

          <section className="employee-panel" id="employees">
            <div className="panel-heading">
              <div>
                <h2>All employees <span className="count-pill">{employees.length}</span></h2>
                <p>A directory of everyone in your organization.</p>
              </div>
              <button className="secondary-button" onClick={() => setRefreshKey((key) => key + 1)} title="Refresh employee list">
                ↻ <span>Refresh</span>
              </button>
            </div>
            <div className="table-toolbar">
              <label className="search-field">
                <span aria-hidden="true">⌕</span>
                <input
                  type="search"
                  placeholder="Search by name..."
                  value={search}
                  onChange={(event) => setSearch(event.target.value)}
                  aria-label="Search employees by name"
                />
                {search && <button onClick={() => setSearch('')} aria-label="Clear search">×</button>}
              </label>
              <label className="select-field">
                <span aria-hidden="true">☷</span>
                <select value={department} onChange={(event) => setDepartment(event.target.value)} aria-label="Filter by department">
                  <option value="">All departments</option>
                  {departmentOptions.map((name) => <option key={name} value={name}>{name}</option>)}
                </select>
                <span className="select-arrow">⌄</span>
              </label>
            </div>

            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>EMPLOYEE</th><th>DEPARTMENT</th><th>JOB TITLE</th><th>SALARY</th><th>JOINED</th><th>STATUS</th><th aria-label="Actions" />
                  </tr>
                </thead>
                <tbody>
                  {loading ? (
                    <tr><td className="table-state" colSpan="7"><span className="spinner" /> Loading employees...</td></tr>
                  ) : rows.length === 0 ? (
                    <tr>
                      <td className="table-state empty-state" colSpan="7">
                        <span className="empty-icon">♙</span>
                        <strong>{search || department ? 'No matching employees' : 'No employees yet'}</strong>
                        <span>{search || department ? 'Try another search or filter.' : 'Add your first employee to get started.'}</span>
                      </td>
                    </tr>
                  ) : rows.map((employee, index) => (
                    <tr key={employee.id}>
                      <td>
                        <div className="employee-cell">
                          <span className={`employee-avatar avatar-${index % 5}`}>{initials(employee)}</span>
                          <span className="employee-details">
                            <strong>{employee.firstName} {employee.lastName}</strong>
                            <span>{employee.email}{employee.phoneNumber ? ` · ${employee.phoneNumber}` : ''}</span>
                          </span>
                        </div>
                      </td>
                      <td><span className="department-label">{employee.department}</span></td>
                      <td className="job-title">{employee.jobTitle}</td>
                      <td className="salary-cell">{formatCurrency(employee.salary)}</td>
                      <td className="date-cell">{formatDate(employee.joiningDate)}</td>
                      <td><span className={`status-badge ${employee.employmentStatus === 'ACTIVE' ? 'is-active' : 'is-inactive'}`}>
                        <i /> {employee.employmentStatus === 'ACTIVE' ? 'Active' : 'Inactive'}
                      </span></td>
                      <td>
                        <div className="row-actions">
                          <button onClick={() => openEditDialog(employee)} aria-label={`Edit ${employee.firstName} ${employee.lastName}`} title="Edit">✎</button>
                          <button className="delete-action" onClick={() => handleDelete(employee)} aria-label={`Delete ${employee.firstName} ${employee.lastName}`} title="Delete">⌫</button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <div className="table-footer">
              <span>Showing <strong>{rows.length}</strong> of <strong>{employees.length}</strong> employees</span>
              <span className="live-indicator"><i /> Live directory</span>
            </div>
          </section>
          <footer className="page-footer">© {new Date().getFullYear()} PeopleDesk <span>•</span> Built for better teams</footer>
        </section>
      </main>

      {dialogOpen && (
        <div className="modal-backdrop" role="presentation" onMouseDown={(event) => {
          if (event.target === event.currentTarget && !saving) setDialogOpen(false);
        }}>
          <section className="employee-modal" role="dialog" aria-modal="true" aria-labelledby="modal-title">
            <div className="modal-heading">
              <div>
                <p className="eyebrow">{editingEmployee ? 'UPDATE RECORD' : 'NEW TEAM MEMBER'}</p>
                <h2 id="modal-title">{editingEmployee ? 'Edit employee' : 'Add employee'}</h2>
              </div>
              <button className="modal-close" onClick={() => setDialogOpen(false)} aria-label="Close dialog">×</button>
            </div>
            <form onSubmit={handleSubmit}>
              <div className="form-grid">
                <label>First name<input name="firstName" required maxLength="80" value={form.firstName} onChange={updateField} /></label>
                <label>Last name<input name="lastName" required maxLength="80" value={form.lastName} onChange={updateField} /></label>
                <label className="full-field">Work email<input type="email" name="email" required maxLength="254" value={form.email} onChange={updateField} /></label>
                <label>Department
                  <input name="department" required maxLength="100" list="department-options" value={form.department} onChange={updateField} />
                  <datalist id="department-options">{departments.map((name) => <option key={name} value={name} />)}</datalist>
                </label>
                <label>Job title<input name="jobTitle" required maxLength="120" value={form.jobTitle} onChange={updateField} /></label>
                <label>Salary (USD)<input type="number" name="salary" required min="0.01" step="0.01" value={form.salary} onChange={updateField} /></label>
                <label>Phone number<input type="tel" name="phoneNumber" maxLength="30" value={form.phoneNumber} onChange={updateField} /></label>
                <label>Joining date<input type="date" name="joiningDate" required max={new Date().toISOString().slice(0, 10)} value={form.joiningDate} onChange={updateField} /></label>
                <label>Employment status
                  <select name="employmentStatus" value={form.employmentStatus} onChange={updateField}>
                    <option value="ACTIVE">Active</option><option value="INACTIVE">Inactive</option>
                  </select>
                </label>
              </div>
              {error && <div className="form-error" role="alert">{error}</div>}
              <div className="modal-actions">
                <button type="button" className="secondary-button" onClick={() => setDialogOpen(false)} disabled={saving}>Cancel</button>
                <button type="submit" className="primary-button" disabled={saving}>
                  {saving && <span className="button-spinner" />}
                  {saving ? 'Saving...' : editingEmployee ? 'Save changes' : 'Add employee'}
                </button>
              </div>
            </form>
          </section>
        </div>
      )}
    </div>
  );
}
