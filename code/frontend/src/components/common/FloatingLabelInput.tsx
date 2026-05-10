import { useState, type ReactNode } from 'react'
import { cn } from '@/lib/utils'

interface FloatingLabelInputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  /** 左侧图标 */
  icon?: ReactNode
  /** 右侧操作区（如密码切换按钮） */
  rightSlot?: ReactNode
  /** 标签文本 */
  label: string
  /** 当前值，用于判断是否上浮 */
  value?: string | number | readonly string[]
  /** 主题强调色 */
  accentColor?: string
  /** 次要文字颜色 */
  textSecondary?: string
}

export default function FloatingLabelInput({
  icon,
  rightSlot,
  label,
  value,
  className,
  id,
  accentColor,
  textSecondary,
  onFocus,
  onBlur,
  ...props
}: FloatingLabelInputProps) {
  const [focused, setFocused] = useState(false)
  const hasValue = value !== undefined && value !== ''
  const floatUp = focused || hasValue

  return (
    <div className="relative">
      {/* Icon */}
      {icon && (
        <div
          className={cn(
            'absolute left-3.5 top-1/2 -translate-y-1/2 z-10 transition-all duration-200 pointer-events-none',
            focused ? 'opacity-100' : 'opacity-60',
          )}
          style={{ color: focused ? (accentColor || '#3b82f6') : (textSecondary || '#94a3b8') }}
        >
          {icon}
        </div>
      )}

      {/* Label - floats up on focus/value */}
      <label
        htmlFor={id}
        className={cn(
          'absolute z-10 transition-all duration-200 pointer-events-none select-none',
          icon ? 'left-10' : 'left-3.5',
          floatUp
            ? 'top-2 text-[10px] font-medium'
            : 'top-1/2 -translate-y-1/2 text-sm',
        )}
        style={{
          color: floatUp
            ? (accentColor || '#3b82f6')
            : (textSecondary || '#94a3b8'),
        }}
      >
        {label}
      </label>

      {/* Input */}
      <input
        id={id}
        value={value}
        onFocus={(e) => {
          setFocused(true)
          onFocus?.(e)
        }}
        onBlur={(e) => {
          setFocused(false)
          onBlur?.(e)
        }}
        className={cn(
          'w-full transition-all duration-200 rounded-lg',
          'pt-5 pb-1.5', // room for floating label
          icon ? 'pl-10' : 'pl-3.5',
          rightSlot ? 'pr-10' : 'pr-3.5',
          focused && 'border-opacity-100',
          className,
        )}
        style={{
          background: 'transparent',
          border: '1px solid',
          borderColor: focused ? (accentColor || '#3b82f6') : 'rgba(255,255,255,0.1)',
          boxShadow: focused
            ? `0 0 0 1px ${accentColor || '#3b82f6'}, 0 0 20px ${accentColor || '#3b82f6'}20`
            : 'none',
          color: textSecondary || '#f1f5f9',
          transition: 'border-color 0.2s ease, box-shadow 0.2s ease',
        } as React.CSSProperties}
        {...props}
      />

      {/* Right slot */}
      {rightSlot && (
        <div className="absolute right-3.5 top-1/2 -translate-y-1/2 z-10">
          {rightSlot}
        </div>
      )}
    </div>
  )
}