import request from '../request'
import type { PageRequest, Message } from '@/types/api'

type ListResponse<T> = { list: T[]; total: number; page: number; pageSize: number }

export function getMessages(params: PageRequest): Promise<ListResponse<Message>> {
  return request.get<ListResponse<Message>>('/operator/messages', { params }) as any
}

export function markMessageRead(id: string | number): Promise<null> {
  return request.put<null>(`/operator/messages/${id}/read`) as any
}