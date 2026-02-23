<template>
  <div class="container">
    <header class="header">
      <h1>👥 Gestión de Clientes</h1>
      <button class="btn-primary" @click="openNewForm" v-if="!showForm">+ Nuevo Cliente</button>
    </header>

    <div v-if="errorMsg" class="alert alert-error">{{ errorMsg }}</div>

    <transition name="fade">
      <ClienteForm
        v-if="showForm"
        :initial-data="selectedCliente"
        @save="handleSave"
        @cancel="closeForm"
      />
    </transition>

    <div v-if="!showForm" style="display: flex; gap: 1.5rem; align-items: flex-start;">
      <!-- Lista -->
      <div class="card content-card" style="flex: 1; min-width: 0;">
        <div class="search-bar">
          <input
            type="text"
            v-model="searchQuery"
            placeholder="Buscar por nombre..."
            @input="handleSearch"
          />
          <button class="btn-primary" @click="loadClientes">Refrescar</button>
        </div>

        <div class="table-responsive">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Nombre Completo</th>
                <th>CUIT</th>
                <th>Email</th>
                <th>Teléfono</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="clientes.length === 0">
                <td colspan="6" style="text-align: center; color: #8b949e;">No hay clientes disponibles.</td>
              </tr>
              <tr
                v-for="c in clientes"
                :key="c.id"
                :class="{ 'row-selected': detailCliente?.id === c.id }"
                style="cursor: pointer;"
                @click="viewDetail(c)"
              >
                <td>{{ c.id }}</td>
                <td>{{ c.nombre }} {{ c.apellido }}</td>
                <td>{{ c.cuit }}</td>
                <td>{{ c.email }}</td>
                <td>{{ c.telefonoCelular }}</td>
                <td class="actions" @click.stop>
                  <button class="btn-primary action-btn" @click="editCliente(c)">✏️</button>
                  <button class="btn-danger action-btn" @click="deleteCliente(c.id!)">🗑️</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Panel de detalle -->
      <transition name="slide">
        <div v-if="detailCliente" class="card detail-panel">
          <div class="detail-header">
            <h3>Detalle del Cliente</h3>
            <button class="btn-close" @click="detailCliente = null">✕</button>
          </div>
          <div class="detail-avatar">{{ initials(detailCliente) }}</div>
          <div class="detail-name">{{ detailCliente.nombre }} {{ detailCliente.apellido }}</div>
          <div class="detail-fields">
            <div class="detail-field">
              <span class="detail-label">CUIT</span>
              <span>{{ detailCliente.cuit }}</span>
            </div>
            <div class="detail-field">
              <span class="detail-label">Razón Social</span>
              <span>{{ detailCliente.razonSocial }}</span>
            </div>
            <div class="detail-field">
              <span class="detail-label">Email</span>
              <span>{{ detailCliente.email }}</span>
            </div>
            <div class="detail-field">
              <span class="detail-label">Teléfono</span>
              <span>{{ detailCliente.telefonoCelular }}</span>
            </div>
            <div class="detail-field">
              <span class="detail-label">Fecha Nac.</span>
              <span>{{ detailCliente.fechaNacimiento }}</span>
            </div>
          </div>
          <div class="detail-actions">
            <button class="btn-primary" style="width: 100%;" @click="editCliente(detailCliente)">✏️ Editar</button>
          </div>
        </div>
      </transition>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { clienteService, type Cliente } from './services/api';
import ClienteForm from './components/ClienteForm.vue';

const clientes = ref<Cliente[]>([]);
const searchQuery = ref('');
const errorMsg = ref('');
const showForm = ref(false);
const selectedCliente = ref<Cliente | null>(null);
const detailCliente = ref<Cliente | null>(null);

let searchTimeout: ReturnType<typeof setTimeout> | null = null;

const loadClientes = async () => {
  try {
    errorMsg.value = '';
    clientes.value = await clienteService.getAll();
  } catch (e: any) {
    errorMsg.value = 'Error al cargar los clientes: ' + (e.response?.data?.message || e.message);
  }
};

const handleSearch = () => {
  if (searchTimeout) clearTimeout(searchTimeout);
  searchTimeout = setTimeout(async () => {
    if (!searchQuery.value.trim()) {
      await loadClientes();
      return;
    }
    try {
      clientes.value = await clienteService.search(searchQuery.value);
    } catch (e: any) {
      errorMsg.value = 'Error en la búsqueda: ' + (e.response?.data?.message || e.message);
    }
  }, 300);
};

const viewDetail = (cliente: Cliente) => {
  detailCliente.value = detailCliente.value?.id === cliente.id ? null : cliente;
};

const initials = (c: Cliente) =>
  `${c.nombre.charAt(0)}${c.apellido.charAt(0)}`.toUpperCase();

const openNewForm = () => {
  selectedCliente.value = null;
  showForm.value = true;
};

const editCliente = (cliente: Cliente) => {
  selectedCliente.value = cliente;
  detailCliente.value = null;
  showForm.value = true;
};

const deleteCliente = async (id: number) => {
  if (!confirm('¿Estás seguro de que deseas eliminar este cliente?')) return;
  try {
    await clienteService.delete(id);
    if (detailCliente.value?.id === id) detailCliente.value = null;
    await loadClientes();
  } catch (e: any) {
    errorMsg.value = 'Error al eliminar: ' + (e.response?.data?.message || e.message);
  }
};

const handleSave = async (data: Cliente) => {
  try {
    errorMsg.value = '';
    if (data.id) {
      await clienteService.update(data.id, data);
    } else {
      await clienteService.create(data);
    }
    closeForm();
    await loadClientes();
  } catch (e: any) {
    if (e.response?.status === 400 && e.response.data) {
      errorMsg.value = e.response.data.message
        ? e.response.data.message
        : 'Campos inválidos: ' + JSON.stringify(e.response.data);
    } else {
      errorMsg.value = 'Error al guardar el cliente: ' + e.message;
    }
  }
};

const closeForm = () => {
  showForm.value = false;
  selectedCliente.value = null;
};

onMounted(loadClientes);
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}
.search-bar {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
}
.search-bar input { flex: 1; }
.table-responsive { overflow-x: auto; }
.actions { display: flex; gap: 0.5rem; }
.action-btn { padding: 0.4rem 0.6rem; font-size: 0.8rem; }
.row-selected { background-color: rgba(100, 108, 255, 0.1) !important; }

/* Detail panel */
.detail-panel {
  width: 280px;
  flex-shrink: 0;
}
.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}
.detail-header h3 { margin: 0; }
.btn-close {
  background: transparent;
  color: #8b949e;
  border: none;
  font-size: 1rem;
  padding: 0.2rem 0.5rem;
  cursor: pointer;
}
.btn-close:hover { color: #fff; }
.detail-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: linear-gradient(135deg, #646cff, #535bf2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.4rem;
  font-weight: 700;
  color: white;
  margin: 0 auto 0.75rem;
}
.detail-name {
  text-align: center;
  font-weight: 600;
  font-size: 1.1rem;
  color: #fff;
  margin-bottom: 1.5rem;
}
.detail-fields { display: flex; flex-direction: column; gap: 0.75rem; }
.detail-field { display: flex; flex-direction: column; gap: 0.2rem; }
.detail-label { font-size: 0.75rem; color: #8b949e; text-transform: uppercase; letter-spacing: 0.05em; }
.detail-actions { margin-top: 1.5rem; }

/* Animations */
.fade-enter-active, .fade-leave-active { transition: opacity 0.3s ease, transform 0.3s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; transform: translateY(-10px); }
.slide-enter-active, .slide-leave-active { transition: opacity 0.25s ease, transform 0.25s ease; }
.slide-enter-from, .slide-leave-to { opacity: 0; transform: translateX(20px); }
</style>
