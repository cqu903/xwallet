export const IdleNotifyPlugin = async ({ $ }) => {
  return {
    event: async ({ event }) => {
      if (event.type === "session.idle") {
        await $`osascript -e 'display notification "AI已完成任务，等待你的输入" with title "OpenCode" sound name "Glass"'`
      }
    },
  }
}
