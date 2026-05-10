import { useState, useEffect } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { ArrowLeft, Package, ShoppingCart } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Card } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { toast } from 'sonner'
import { getProduct, getAddresses, createOrder, type Product, type Address } from '@/services/user'

export default function ProductDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [loading, setLoading] = useState(true)
  const [product, setProduct] = useState<Product | null>(null)
  const [addresses, setAddresses] = useState<Address[]>([])
  const [selectedAddressId, setSelectedAddressId] = useState<string>('')
  const [ordering, setOrdering] = useState(false)

  useEffect(() => {
    const fetchData = async () => {
      if (!id) return
      setLoading(true)
      try {
        const [productRes, addressRes] = await Promise.all([
          getProduct(id),
          getAddresses(),
        ])
        setProduct(productRes)
        setAddresses(addressRes)
        // 默认选中新地址
        const defaultAddr = addressRes.find((a) => a.isDefault === 1)
        if (defaultAddr) {
          setSelectedAddressId(defaultAddr.id)
        } else if (addressRes.length > 0) {
          setSelectedAddressId(addressRes[0].id)
        }
      } catch (err: any) {
        toast.error(err.message || '获取商品详情失败')
      } finally {
        setLoading(false)
      }
    }
    fetchData()
  }, [id])

  const handleOrder = async () => {
    if (!product || !selectedAddressId) return
    if (product.stock <= 0) {
      toast.error('库存不足')
      return
    }
    setOrdering(true)
    try {
      await createOrder({
        productId: product.id,
        addressId: selectedAddressId,
      })
      toast.success('下单成功')
      navigate('/user/orders')
    } catch (err: any) {
      toast.error(err.message || '下单失败')
    } finally {
      setOrdering(false)
    }
  }

  if (loading) {
    return (
      <div className="max-w-3xl mx-auto p-4">
        <Skeleton className="h-6 w-32 mb-6" />
        <Card>
          <div className="flex gap-6 p-6">
            <Skeleton className="w-64 h-64" />
            <div className="flex-1 space-y-4">
              <Skeleton className="h-8 w-3/4" />
              <Skeleton className="h-4 w-full" />
              <Skeleton className="h-4 w-2/3" />
              <Skeleton className="h-10 w-32" />
            </div>
          </div>
        </Card>
      </div>
    )
  }

  if (!product) {
    return (
      <div className="max-w-3xl mx-auto p-4 text-center py-12">
        <p className="text-muted-foreground">商品不存在</p>
        <Link to="/user/products">
          <Button className="mt-4">返回商品列表</Button>
        </Link>
      </div>
    )
  }

  return (
    <div className="max-w-3xl mx-auto p-4">
      {/* Back Button */}
      <Link to="/user/products">
        <Button variant="ghost" className="mb-6 gap-2">
          <ArrowLeft className="w-4 h-4" />
          返回商品列表
        </Button>
      </Link>

      <Card>
        <div className="flex flex-col sm:flex-row gap-6 p-6">
          {/* Product Image */}
          <div className="w-full sm:w-64 h-64 bg-muted rounded-lg flex-shrink-0 overflow-hidden">
            {product.image ? (
              <img src={product.image} alt={product.name} className="w-full h-full object-cover" />
            ) : (
              <div className="w-full h-full flex items-center justify-center">
                <Package className="w-16 h-16 text-muted-foreground/50" />
              </div>
            )}
          </div>

          {/* Product Info */}
          <div className="flex-1 space-y-4">
            <h1 className="text-xl font-semibold">{product.name}</h1>
            <p className="text-sm text-muted-foreground">{product.description}</p>
            <div className="flex items-center gap-4">
              <span className="text-2xl font-bold text-accent">{product.points} 积分</span>
              <span className={`text-sm ${product.stock > 0 ? 'text-green-600' : 'text-red-500'}`}>
                {product.stock > 0 ? `库存 ${product.stock}` : '已售罄'}
              </span>
            </div>

            {/* Address Selection */}
            {addresses.length > 0 ? (
              <div className="space-y-2">
                <p className="text-sm font-medium">配送至</p>
                <select
                  value={selectedAddressId}
                  onChange={(e) => setSelectedAddressId(e.target.value)}
                  className="w-full border rounded-lg px-3 py-2 text-sm bg-background"
                >
                  {addresses.map((addr) => (
                    <option key={addr.id} value={addr.id}>
                      {addr.name} {addr.phone} {addr.province}{addr.city}{addr.district}{addr.detail}
                    </option>
                  ))}
                </select>
              </div>
            ) : (
              <div className="text-sm text-muted-foreground">
                <Link to="/user/addresses" className="text-accent hover:underline">
                  请先添加收货地址
                </Link>
              </div>
            )}

            {/* Order Button */}
            <Button
              className="w-full sm:w-auto gap-2"
              disabled={product.stock <= 0 || !selectedAddressId || ordering}
              onClick={handleOrder}
            >
              {ordering ? (
                '下单中...'
              ) : (
                <>
                  <ShoppingCart className="w-4 h-4" />
                  立即兑换
                </>
              )}
            </Button>
          </div>
        </div>
      </Card>
    </div>
  )
}