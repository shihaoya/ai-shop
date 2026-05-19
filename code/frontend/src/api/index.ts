// 模块统一导出
export * from './modules/auth'
export * from './modules/user'
export * from './modules/shop'
export * from './modules/product'
export * from './modules/order'
export * from './modules/message'
export * from './modules/admin'
export * from './modules/operator'
export * from './modules/operatorUser'

// 文件上传相关
export { uploadFile, uploadProductImage, deleteFile, getFile, getFilesByBusiness } from './modules/file/index'
export type { FileRecord } from './modules/file/index'