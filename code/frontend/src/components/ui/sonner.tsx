import { useTheme } from "next-themes"
import { Toaster as Sonner, type ToasterProps } from "sonner"
import { CircleCheckIcon, InfoIcon, TriangleAlertIcon, OctagonXIcon, Loader2Icon } from "lucide-react"

const Toaster = ({ ...props }: ToasterProps) => {
  const { theme = "system" } = useTheme()

  return (
    <Sonner
      theme={theme as ToasterProps["theme"]}
      className="toaster group"
      position="top-center"
      icons={{
        success: (
          <CircleCheckIcon className="size-5 text-green-500" />
        ),
        info: (
          <InfoIcon className="size-5 text-blue-500" />
        ),
        warning: (
          <TriangleAlertIcon className="size-5 text-amber-500" />
        ),
        error: (
          <OctagonXIcon className="size-5 text-red-500" />
        ),
        loading: (
          <Loader2Icon className="size-5 text-blue-500 animate-spin" />
        ),
      }}
      style={
        {
          // 亮暗模式使用同一个变量，通过 data-theme 切换
          "--toast-bg": "var(--card-bg)",
          "--toast-border": "var(--accent)",
          "--toast-text": "var(--text-primary)",
          "--toast-shadow": "0 8px 32px rgba(0, 0, 0, 0.3)",
          "--toast-radius": "14px",
        } as React.CSSProperties
      }
      toastOptions={{
        classNames: {
          toast: "group toaster:backdrop-blur-md border shadow-lg",
          success: "border-green-500/40 bg-green-500/10 text-green-500",
          error: "border-red-500/40 bg-red-500/10 text-red-500",
          warning: "border-amber-500/40 bg-amber-500/10 text-amber-500",
          info: "border-blue-500/40 bg-blue-500/10 text-blue-500",
          loading: "border-blue-500/40 bg-blue-500/10 text-blue-500",
          title: "text-sm font-semibold text-[var(--text-primary)]",
          description: "text-xs text-[var(--text-secondary)]",
          closeButton: "opacity-0 group-hover:opacity-100 transition-opacity text-[var(--text-secondary)]",
          loader: "text-blue-500",
          icon: "text-inherit",
        },
      }}
      {...props}
    />
  )
}

export { Toaster }
