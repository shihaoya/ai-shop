import request from '../request'
import type { PageRequest, Message } from '@/types'

type ListResponse<T> = { list: T[]; total: number; page: number; pageSize: number }

export function getMessages(params: PageRequest): Promise<ListResponse<Message>> {
  return request.get<ListResponse<Message>>('/operator/messages', { params }) as any
}

export function markMessageRead(id: string | number): Promise<null> {
  return request.put<null>(`/operator/messages/${id}/read`) as any
}

export function createMessage(data: { title: string; content: string; userId?: string }): Promise<null> {
  return request.post<null>('/operator/messages', data) as any
}

export function deleteMessage(id: string | number): Promise<null> {
  return request.delete<null>(`/operator/messages/${id}`) as any
}