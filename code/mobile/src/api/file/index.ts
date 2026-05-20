import request from '../request'
import type { ApiResult } from '@/types'

export interface FileRecord {
  id: string
  fileName: string
  filePath: string
  fileSize: number
  fileType: string
  fileExt: string
  storageType: number
  businessType: string
  businessId: number
  url: string
  createdAt: string
}

/**
 * 上传文件
 * @param file 文件对象
 * @param businessType 业务类型: product, avatar 等
 * @param businessId 关联业务ID(可选)
 */
export function uploadFile(
  file: File,
  businessType: string,
  businessId?: number
): Promise<FileRecord> {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('businessType', businessType)
  if (businessId) {
    formData.append('businessId', businessId.toString())
  }
  return request.post<FileRecord>('/file/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  }) as any
}

/**
 * 上传商品图片
 * @param file 文件对象
 */
export function uploadProductImage(file: File): Promise<FileRecord> {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<FileRecord>('/file/upload/product', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  }) as any
}

/**
 * 删除文件
 * @param fileId 文件ID
 */
export function deleteFile(fileId: string | number): Promise<null> {
  return request.delete<null>(`/file/${fileId}`) as any
}

/**
 * 获取文件信息
 * @param fileId 文件ID
 */
export function getFile(fileId: string | number): Promise<FileRecord> {
  return request.get<FileRecord>(`/file/${fileId}`) as any
}

/**
 * 根据业务查询文件列表
 * @param businessType 业务类型
 * @param businessId 业务ID
 */
export function getFilesByBusiness(
  businessType: string,
  businessId: string | number
): Promise<FileRecord[]> {
  return request.get<FileRecord[]>('/file/list', {
    params: { businessType, businessId },
  }) as any
}