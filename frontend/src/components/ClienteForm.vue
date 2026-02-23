<template>
  <div class="card form-container">
    <h2>{{ isEditing ? 'Editar Cliente' : 'Nuevo Cliente' }}</h2>

    <div v-if="formError" class="alert alert-error" style="margin-bottom: 1rem;">
      {{ formError }}
    </div>

    <form @submit.prevent="submitForm" novalidate>
      <div class="form-row">
        <div class="form-group">
          <label for="nombre">Nombre *</label>
          <input id="nombre" v-model="form.nombre" type="text" :class="{ 'input-error': errors.nombre }" @input="maskCapitalize('nombre', $event)" />
          <span class="field-error" v-if="errors.nombre">{{ errors.nombre }}</span>
        </div>
        <div class="form-group">
          <label for="apellido">Apellido *</label>
          <input id="apellido" v-model="form.apellido" type="text" :class="{ 'input-error': errors.apellido }" @input="maskCapitalize('apellido', $event)" />
          <span class="field-error" v-if="errors.apellido">{{ errors.apellido }}</span>
        </div>
      </div>

      <div class="form-row">
        <div class="form-group">
          <label for="cuit">CUIT *</label>
          <input
            id="cuit"
            v-model="form.cuit"
            type="text"
            placeholder="20-12345678-9"
            :disabled="isEditing"
            :class="{ 'input-error': errors.cuit, 'input-disabled': isEditing }"
            @input="maskCuit"
            maxlength="13"
          />
          <span class="field-error" v-if="errors.cuit">{{ errors.cuit }}</span>
        </div>
        <div class="form-group">
          <label for="razonSocial">Razón Social *</label>
          <input id="razonSocial" v-model="form.razonSocial" type="text" :class="{ 'input-error': errors.razonSocial }" />
          <span class="field-error" v-if="errors.razonSocial">{{ errors.razonSocial }}</span>
        </div>
      </div>

      <div class="form-row">
        <div class="form-group">
          <label for="email">Email *</label>
          <input id="email" v-model="form.email" type="email" :class="{ 'input-error': errors.email }" @input="maskLowercase($event)" />
          <span class="field-error" v-if="errors.email">{{ errors.email }}</span>
        </div>
        <div class="form-group">
          <label for="telefono">Teléfono Celular *</label>
          <input
            id="telefono"
            v-model="form.telefonoCelular"
            type="tel"
            placeholder="Ej: 1165874210"
            :class="{ 'input-error': errors.telefonoCelular }"
            @input="maskPhone"
            maxlength="10"
          />
          <span class="field-error" v-if="errors.telefonoCelular">{{ errors.telefonoCelular }}</span>
        </div>
      </div>

      <div class="form-group">
        <label for="nacimiento">Fecha de Nacimiento *</label>
        <input id="nacimiento" v-model="form.fechaNacimiento" type="date" :class="{ 'input-error': errors.fechaNacimiento }" />
        <span class="field-error" v-if="errors.fechaNacimiento">{{ errors.fechaNacimiento }}</span>
      </div>

      <div class="form-actions">
        <button type="button" class="btn-danger" @click="$emit('cancel')">Cancelar</button>
        <button type="button" class="btn-secondary" @click="resetForm">Limpiar</button>
        <button type="submit" class="btn-primary">Guardar</button>
      </div>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import type { Cliente } from '../services/api';

const props = defineProps<{ initialData?: Cliente | null }>();
const emit = defineEmits<{
  (e: 'save', data: Cliente): void;
  (e: 'cancel'): void;
}>();

const isEditing = ref(false);
const formError = ref('');
const errors = ref<Record<string, string>>({});

const emptyForm = (): Cliente => ({
  nombre: '', apellido: '', razonSocial: '', cuit: '',
  fechaNacimiento: '', telefonoCelular: '', email: ''
});

const form = ref<Cliente>(emptyForm());

watch(() => props.initialData, (newVal) => {
  if (newVal) {
    isEditing.value = true;
    form.value = { ...newVal };
  } else {
    isEditing.value = false;
    form.value = emptyForm();
  }
  errors.value = {};
  formError.value = '';
}, { immediate: true });

const maskCapitalize = (field: 'nombre' | 'apellido', e: Event) => {
  const val = (e.target as HTMLInputElement).value;
  form.value[field] = val.replace(/(^|\s)\S/g, c => c.toUpperCase());
};

const maskLowercase = (e: Event) => {
  form.value.email = (e.target as HTMLInputElement).value.toLowerCase();
};

const maskCuit = (e: Event) => {
  let val = (e.target as HTMLInputElement).value.replace(/\D/g, '');
  if (val.length > 2) val = val.slice(0, 2) + '-' + val.slice(2);
  if (val.length > 11) val = val.slice(0, 11) + '-' + val.slice(11);
  form.value.cuit = val.slice(0, 13);
};

const maskPhone = (e: Event) => {
  form.value.telefonoCelular = (e.target as HTMLInputElement).value.replace(/\D/g, '').slice(0, 10);
};

const validate = (): boolean => {
  const e: Record<string, string> = {};
  if (!form.value.nombre.trim()) e.nombre = 'El nombre es obligatorio';
  if (!form.value.apellido.trim()) e.apellido = 'El apellido es obligatorio';
  if (!form.value.razonSocial.trim()) e.razonSocial = 'La razón social es obligatoria';
  if (!form.value.telefonoCelular.trim()) {
    e.telefonoCelular = 'El teléfono es obligatorio';
  } else if (form.value.telefonoCelular.length !== 10) {
    e.telefonoCelular = 'El teléfono debe tener 10 dígitos';
  }
  if (!form.value.fechaNacimiento) e.fechaNacimiento = 'La fecha de nacimiento es obligatoria';

  if (!form.value.email.trim()) {
    e.email = 'El email es obligatorio';
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.value.email)) {
    e.email = 'El formato del email no es válido';
  }

  if (!isEditing.value) {
    if (!form.value.cuit.trim()) {
      e.cuit = 'El CUIT es obligatorio';
    } else if (!/^\d{2}-\d{8}-\d{1}$/.test(form.value.cuit)) {
      e.cuit = 'Formato inválido. Use XX-XXXXXXXX-X';
    }
  }

  errors.value = e;
  return Object.keys(e).length === 0;
};

const resetForm = () => {
  form.value = isEditing.value ? { ...props.initialData! } : emptyForm();
  errors.value = {};
  formError.value = '';
};

const submitForm = () => {
  formError.value = '';
  if (!validate()) return;
  emit('save', { ...form.value });
};
</script>

<style scoped>
.form-container { margin-bottom: 2rem; }
.form-row { display: flex; gap: 1rem; }
.form-row > * { flex: 1; }
.form-actions { display: flex; justify-content: flex-end; gap: 1rem; margin-top: 1.5rem; }
.field-error { color: #f85149; font-size: 0.8rem; margin-top: 0.25rem; }
.input-error { border-color: #f85149 !important; }
.btn-secondary { background: transparent; border: 1px solid #8b949e; color: #8b949e; }
.btn-secondary:hover { border-color: #fff; color: #fff; }
</style>
