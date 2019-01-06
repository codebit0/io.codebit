package io.codebit.support.image.options;


public abstract class AbWriteOption implements IWriteOption
{
	private boolean ignoreMetadata = false;
	
	public boolean ignoreMetadata()
	{
		return this.ignoreMetadata;
	}
	
	public IWriteOption ignoreMetadata(boolean ignoreMetadata)
	{
		this.ignoreMetadata = ignoreMetadata;
		return this;
	}
}
