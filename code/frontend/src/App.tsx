import { RouterProvider } from "react-router-dom"
import { router } from "./router"
import { Toaster } from "@/components/ui/sonner"
import React from "react"

class ErrorBoundary extends React.Component<
  { children: React.ReactNode },
  { hasError: boolean; error: Error | null }
> {
  constructor(props: { children: React.ReactNode }) {
    super(props)
    this.state = { hasError: false, error: null }
  }

  static getDerivedStateFromError(error: Error) {
    return { hasError: true, error }
  }

  componentDidCatch(error: Error, info: React.ErrorInfo) {
    console.error("ErrorBoundary caught:", error, info)
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="min-h-screen flex items-center justify-center bg-neutral-50">
          <div className="text-center p-8 bg-white rounded-lg shadow-lg max-w-md">
            <h1 className="text-xl font-bold text-red-600 mb-4">应用错误</h1>
            <p className="text-neutral-600 mb-4">{this.state.error?.message || String(this.state.error)}</p>
            <button
              type="button"
              onClick={() => window.location.href = '/auth/login'}
              className="px-4 py-2 bg-accent text-white rounded hover:bg-accent/90"
            >
              返回登录页
            </button>
          </div>
        </div>
      )
    }
    return this.props.children
  }
}

function App() {
  return (
    <ErrorBoundary>
      <RouterProvider router={router} />
      <Toaster />
    </ErrorBoundary>
  )
}

export default App