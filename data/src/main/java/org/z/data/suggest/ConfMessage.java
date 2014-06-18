package org.z.data.suggest;



public class ConfMessage {
	public int mReturnNumber = 0;//返回结果数目
	public int mMiddleNumber = 0;//在前缀匹配结果数目不够的情况下，最多搜索到几个中间匹配的结果数目  中间匹配： “李白”可以中间匹配上“唐朝李白选集”和“唐朝李白”
	public int mShopRate = 0;    //词汇匹配店铺名词，所占名称的百分比  例如： “李白“在“小李白选集”中占40的百分比
	public int mHzLen = 0; //缓存的中文key长度
	public int mPyLen = 0; //缓存的拼音key长度
	public int mMaxMiddle = 0;//记录最大中间位置数目
}
