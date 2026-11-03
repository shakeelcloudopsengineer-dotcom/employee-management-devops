import axios from 'axios';

const client = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  headers: { 'Content-Type': 'application/json' },
});

export const getEmployees = async () => (await client.get('/employees')).data;

export const getEmployeesByName = async (name) =>
  (await client.get('/employees/search', { params: { name } })).data;

export const getEmployeesByDepartment = async (department) =>
  (await client.get(`/employees/department/${encodeURIComponent(department)}`)).data;

export const createEmployee = async (employee) =>
  (await client.post('/employees', employee)).data;

export const updateEmployee = async (id, employee) =>
  (await client.put(`/employees/${id}`, employee)).data;

export const deleteEmployee = async (id) => client.delete(`/employees/${id}`);

export const getApiErrorMessage = (error) => {
  const validationErrors = error.response?.data?.validationErrors;
  if (validationErrors && Object.keys(validationErrors).length > 0) {
    return Object.values(validationErrors).join('. ');
  }
  return error.response?.data?.message || error.message || 'Something went wrong.';
};
