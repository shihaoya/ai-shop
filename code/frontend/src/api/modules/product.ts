import request from '../request'
import type { PageRequest, Product, Category } from '@/types/api'

type ListResponse<T> = { list: T[]; total: number; page: number; pageSize: number }

export function getCategories() {
  return request.get<Category[]>('/operator/categories') as any
}

export function createCategory(name: string, sort?: number): Promise<Category> {
  return request.post<Category>('/operator/categories', { name, sort }) as any
}

export function updateCategory(id: string | number, name?: string, sort?: number): Promise<null> {
  return request.put<null>(`/operator/categories/${id}`, { name, sort }) as any
}

export function deleteCategory(id: string | number): Promise<null> {
  return request.delete<null>(`/operator/categories/${id}`) as any
}

export function getProducts(params: PageRequest & { keyword?: string }): Promise<ListResponse<Product>> {
  return request.get<ListResponse<Product>>('/operator/products', { params }) as any
}

export function createProduct(data: {
  name: string
  categoryId: string | number
  type: number
  price: number
  stock: number
  limitPerUser?: number
  mainImage?: string
  detailImages?: string
  description?: string
}): Promise<Product> {
  return request.post<Product>('/operator/products', data) as any
}

export function getProduct(id: string | number): Promise<Product> {
  return request.get<Product>(`/operator/products/${id}`) as any
}

export function updateProduct(id: string | number, data: {
  name?: string
  categoryId?: string | number
  type?: number
  price?: number
  stock?: number
  limitPerUser?: number
  mainImage?: string
  detailImages?: string
  description?: string
  status?: number
}): Promise<null> {
  return request.put<null>(`/operator/products/${id}`, data) as any
}

export function deleteProduct(id: string | number): Promise<null> {
  return request.delete<null>(`/operator/products/${id}`) as any
}