import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { Search, Grid, List, Package } from 'lucide-react'
import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { toast } from 'sonner'
import { getProducts, type Product } from '@/services/user'

export default function ProductsPage() {
  const [viewMode, setViewMode] = useState<'card' | 'table'>('card')
  const [loading, setLoading] = useState(true)
  const [products, setProducts] = useState<Product[]>([])
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(1)
  const [pageSize] = useState(12)
  const [keyword, setKeyword] = useState('')
  const [searchKeyword, setSearchKeyword] = useState('')

  const fetchProducts = async () => {
    setLoading(true)
    try {
      const res = await getProducts({
        page,
        pageSize,
        keyword: searchKeyword || undefined,
      })
      setProducts(res.list)
      setTotal(res.total)
    } catch (err: any) {
      toast.error(err.message || '获取商品列表失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchProducts()
  }, [page, searchKeyword])

  const handleSearch = () => {
    setPage(1)
    setSearchKeyword(keyword)
  }

  const totalPages = Math.ceil(total / pageSize)

  return (
    <div className="max-w-6xl mx-auto p-4">
      {/* Search Bar */}
      <div className="flex gap-2 mb-6">
        <div className="relative flex-1 max-w-md">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
          <Input
            placeholder="搜索商品..."
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
            className="pl-9 glass-input"
          />
        </div>
        <Button onClick={handleSearch} className="glass-btn-primary">搜索</Button>
      </div>

      {/* View Toggle */}
      <div className="flex justify-between items-center mb-4">
        <p className="text-sm text-muted-foreground">
          共 {total} 件商品
        </p>
        <div className="flex gap-1 glass rounded-lg p-1">
          <Button
            variant={viewMode === 'card' ? 'secondary' : 'ghost'}
            size="sm"
            onClick={() => setViewMode('card')}
            className={viewMode === 'card' ? 'glass-btn' : 'glass-btn'}
          >
            <Grid className="w-4 h-4" />
          </Button>
          <Button
            variant={viewMode === 'table' ? 'secondary' : 'ghost'}
            size="sm"
            onClick={() => setViewMode('table')}
            className={viewMode === 'table' ? 'glass-btn' : 'glass-btn'}
          >
            <List className="w-4 h-4" />
          </Button>
        </div>
      </div>

      {/* Product Grid/Card View */}
      {loading ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          {Array.from({ length: 8 }).map((_, i) => (
            <Card key={i} className="glass-card">
              <Skeleton className="h-40 w-full rounded-none glass-skeleton" />
              <CardHeader>
                <Skeleton className="h-4 w-3/4 glass-skeleton" />
              </CardHeader>
              <CardContent>
                <Skeleton className="h-3 w-1/2 glass-skeleton" />
              </CardContent>
              <CardFooter>
                <Skeleton className="h-8 w-full glass-skeleton" />
              </CardFooter>
            </Card>
          ))}
        </div>
      ) : products.length === 0 ? (
        <div className="text-center py-12 text-muted-foreground glass-card">
          <Package className="w-12 h-12 mx-auto mb-4 opacity-50" />
          <p>暂无商品</p>
        </div>
      ) : viewMode === 'card' ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          {products.map((product) => (
            <Link key={product.id} to={`/user/products/${product.id}`}>
              <Card className="glass-card hover:ring-2 hover:ring-accent/50 transition-all cursor-pointer h-full">
                <div className="aspect-square bg-muted flex items-center justify-center overflow-hidden">
                  {product.image ? (
                    <img src={product.image} alt={product.name} className="w-full h-full object-cover" />
                  ) : (
                    <Package className="w-12 h-12 text-muted-foreground/50" />
                  )}
                </div>
                <CardHeader className="pb-2">
                  <CardTitle className="text-sm line-clamp-2">{product.name}</CardTitle>
                </CardHeader>
                <CardContent className="pb-2">
                  <p className="text-xs text-muted-foreground line-clamp-2">{product.description}</p>
                </CardContent>
                <CardFooter className="pt-0">
                  <div className="flex items-center justify-between w-full">
                    <span className="text-accent font-semibold">{product.points} 积分</span>
                    <span className="text-xs text-muted-foreground">库存 {product.stock}</span>
                  </div>
                </CardFooter>
              </Card>
            </Link>
          ))}
        </div>
      ) : (
        <div className="glass-card overflow-hidden">
          <table className="w-full">
            <thead className="bg-muted/30">
              <tr>
                <th className="px-4 py-3 text-left text-sm font-medium">商品</th>
                <th className="px-4 py-3 text-left text-sm font-medium">描述</th>
                <th className="px-4 py-3 text-right text-sm font-medium">积分</th>
                <th className="px-4 py-3 text-right text-sm font-medium">库存</th>
                <th className="px-4 py-3 text-center text-sm font-medium">操作</th>
              </tr>
            </thead>
            <tbody>
              {products.map((product) => (
                <tr key={product.id} className="border-t border-muted/30 hover:bg-muted/20 transition-colors">
                  <td className="px-4 py-3">
                    <div className="flex items-center gap-3">
                      <div className="w-12 h-12 rounded bg-muted flex-shrink-0 overflow-hidden">
                        {product.image ? (
                          <img src={product.image} alt={product.name} className="w-full h-full object-cover" />
                        ) : (
                          <Package className="w-6 h-6 m-auto text-muted-foreground/50" />
                        )}
                      </div>
                      <span className="font-medium text-sm">{product.name}</span>
                    </div>
                  </td>
                  <td className="px-4 py-3 text-sm text-muted-foreground">{product.description}</td>
                  <td className="px-4 py-3 text-sm text-accent font-medium text-right">{product.points}</td>
                  <td className="px-4 py-3 text-sm text-muted-foreground text-right">{product.stock}</td>
                  <td className="px-4 py-3 text-center">
                    <Link to={`/user/products/${product.id}`}>
                      <Button variant="ghost" size="sm" className="glass-btn">查看</Button>
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex justify-center gap-2 mt-6">
          <Button
            variant="outline"
            size="sm"
            disabled={page === 1}
            onClick={() => setPage(page - 1)}
            className="glass-btn"
          >
            上一页
          </Button>
          <span className="px-3 py-2 text-sm glass">
            {page} / {totalPages}
          </span>
          <Button
            variant="outline"
            size="sm"
            disabled={page === totalPages}
            onClick={() => setPage(page + 1)}
            className="glass-btn"
          >
            下一页
          </Button>
        </div>
      )}
    </div>
  )
}