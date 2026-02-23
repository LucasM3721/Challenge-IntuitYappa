import axios from 'axios';

const API_URL = import.meta.env.VITE_API_URL;

export interface Cliente {
  id?: number;
  nombre: string;
  apellido: string;
  razonSocial: string;
  cuit: string;
  fechaNacimiento: string;
  telefonoCelular: string;
  email: string;
}

export const clienteService = {
  async getAll(): Promise<Cliente[]> {
    const response = await axios.get(API_URL);
    return response.data;
  },

  async getById(id: number): Promise<Cliente> {
    const response = await axios.get(`${API_URL}/${id}`);
    return response.data;
  },

  async search(name: string): Promise<Cliente[]> {
    const response = await axios.get(`${API_URL}/search`, { params: { name } });
    return response.data;
  },

  async create(cliente: Cliente): Promise<Cliente> {
    const response = await axios.post(API_URL, cliente);
    return response.data;
  },

  async update(id: number, cliente: Cliente): Promise<Cliente> {
    const response = await axios.put(`${API_URL}/${id}`, cliente);
    return response.data;
  },

  async delete(id: number): Promise<void> {
    await axios.delete(`${API_URL}/${id}`);
  }
};
